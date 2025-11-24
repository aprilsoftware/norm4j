/*
 * Copyright 2025 April Software
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.norm4j.schema;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.norm4j.Query;
import org.norm4j.SelectQueryBuilder;
import org.norm4j.TableManager;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;

public class SchemaSynchronizer {
    private static final Logger LOGGER = Logger.getLogger(SchemaSynchronizer.class.getName());
    private final List<VersionBuilder> versionBuilders;
    private final TableManager tableManager;
    private String schemaVersionTable;
    private String schema;
    private String databaseResourcePath;
    private boolean startFromFirstVersion;

    public SchemaSynchronizer(TableManager tableManager) {
        this.tableManager = tableManager;

        versionBuilders = new ArrayList<>();

        databaseResourcePath = "db";
    }

    public SchemaSynchronizer schemaVersionTable(String schemaVersionTable) {
        this.schemaVersionTable = schemaVersionTable;
        return this;
    }

    public SchemaSynchronizer schema(String schemaName) {
        this.schema = schemaName;
        return this;
    }

    public SchemaSynchronizer databaseResourcePath(String databaseResourcePath) {
        this.databaseResourcePath = databaseResourcePath;
        return this;
    }

    public VersionBuilder version(String name) {
        VersionBuilder builder;

        builder = new VersionBuilder(this, name);

        versionBuilders.add(builder);

        return builder;
    }

    public SchemaSynchronizer startFromFirstVersion(boolean startFromFirstVersion) {
        this.startFromFirstVersion = startFromFirstVersion;
        return this;
    }

    public void apply() {
        ColumnMetadata column;

        tableManager.getMetadataManager().registerTable(SchemaVersion.class, schema, schemaVersionTable);

        column = tableManager.getMetadataManager().getColumnMetadata(SchemaVersion.class, "creationDate");

        try (Connection connection = tableManager.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            if (!tableManager.getMetadataManager().getDialect()
                    .tableExists(connection, column.getTable().getSchema(),
                            column.getTable().getTableName())) {
                try {
                    executeQuery(tableManager,
                            connection,
                            tableManager.getDialect().createTable(column.getTable()));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "SchemaVersion table creation failed.", e);
                }
            }

            try (PreparedStatement lockStatement = tableManager.getDialect()
                    .createLockStatement(connection, column.getTable())) {
                lockStatement.executeQuery();

                if (!startFromFirstVersion &&
                        !versionBuilders.isEmpty() &&
                        !hasAnyAppliedVersion(tableManager, connection)) {
                    VersionBuilder last = versionBuilders.get(versionBuilders.size() - 1);

                    if (!isAlreadyApplied(tableManager, connection, last.name)) {
                        if (last.schemaBuilder.autoCreationEnabled) {
                            createSchema(connection, last.name);
                        }

                        for (Object statement : last.initialStatements) {
                            executeQuery(tableManager, connection, statement);
                        }

                        insertSchemaVersion(connection, column, last.name, last.description);
                    }
                } else {
                    VersionBuilder previousVersionBuilder = null;

                    for (VersionBuilder versionBuilder : versionBuilders) {
                        if (!isAlreadyApplied(tableManager, connection, versionBuilder.name)) {
                            if (previousVersionBuilder == null) {
                                createSchema(connection, versionBuilder.name);
                            } else if (versionBuilder.schemaBuilder.autoMigrationEnabled) {
                                migrateFromPreviousVersion(connection, previousVersionBuilder.name,
                                        versionBuilder.name);
                            }

                            for (Object statement : versionBuilder.statements) {
                                executeQuery(tableManager, connection, statement);
                            }

                            insertSchemaVersion(connection, column, versionBuilder.name, versionBuilder.description);
                        }

                        previousVersionBuilder = versionBuilder;
                    }
                }
            }

            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createSchema(Connection connection, String version) {

        Schema schema;

        schema = Schema.loadFromResource(databaseResourcePath
                + "/"
                + version
                + "/"
                + "schema.json");

        for (String ddl : new SchemaComparator()
                .generateDdl(null, schema, tableManager.getDialect())) {
            tableManager.execute(connection, ddl);
        }
    }

    private void migrateFromPreviousVersion(Connection connection, String fromVersion, String toVersion) {
        Schema fromSchema;
        Schema toSchema;

        fromSchema = Schema.loadFromResource(databaseResourcePath
                + "/"
                + fromVersion
                + "/"
                + "schema.json");

        toSchema = Schema.loadFromResource(databaseResourcePath
                + "/"
                + toVersion
                + "/"
                + "schema.json");

        for (String ddl : new SchemaComparator()
                .generateDdl(fromSchema, toSchema, tableManager.getDialect())) {
            tableManager.execute(connection, ddl);
        }
    }

    private void insertSchemaVersion(Connection connection, ColumnMetadata column, String name, String description)
            throws SQLException {
        try (PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO "
                        + tableManager.getDialect().getTableName(column.getTable())
                        + " (name, description, creationdate)"
                        + " VALUES (?, ?, ?)")) {
            insertStatement.setString(1, name);
            insertStatement.setString(2, description);
            insertStatement.setObject(3,
                    tableManager.getDialect().toSqlValue(column,
                            new Date(System.currentTimeMillis())));
            insertStatement.executeUpdate();
        }
    }

    private void executeQuery(TableManager tableManager, Connection connection, Object statement) {
        if (statement instanceof String) {
            tableManager.execute(connection, (String) statement);
        } else if (statement instanceof Query) {
            ((Query) statement).executeUpdate(connection);
        } else {
            throw new RuntimeException("Unexpected statement type.");
        }
    }

    private boolean isAlreadyApplied(TableManager tableManager, Connection connection, String name) {
        SchemaVersion version;

        version = new SelectQueryBuilder(tableManager)
                .select()
                .from(SchemaVersion.class)
                .where(SchemaVersion::getName, "=", name)
                .getSingleResult(connection, SchemaVersion.class);

        if (version == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean hasAnyAppliedVersion(TableManager tableManager, Connection connection) {
        SchemaVersion version = new SelectQueryBuilder(tableManager)
                .select()
                .from(SchemaVersion.class)
                .limit(1)
                .orderBy(SchemaVersion::getCreationDate)
                .getSingleResult(connection, SchemaVersion.class);

        return version != null;
    }

    public class VersionBuilder {
        private final SchemaBuilder schemaBuilder;
        private final SchemaSynchronizer synchronizer;
        private final List<Object> initialStatements;
        private final List<Object> statements;
        private final String name;
        private String description;

        public VersionBuilder(SchemaSynchronizer synchronizer, String name) {
            this.synchronizer = synchronizer;

            this.name = name;

            this.initialStatements = new ArrayList<>();

            statements = new ArrayList<>();

            schemaBuilder = new SchemaBuilder(this);
        }

        public VersionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SchemaBuilder schema() {
            return schemaBuilder;
        }

        public VersionBuilder execute(String statement) {
            execute(statement, null);
            return this;
        }

        public VersionBuilder execute(String statement, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                statements.add(statement);
            }
            return this;
        }

        public VersionBuilder execute(Query statement) {
            execute(statement, null);
            return this;
        }

        public VersionBuilder execute(Query statement, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                statements.add(statement);
            }
            return this;
        }

        public VersionBuilder executeResource(String resourcePath) {
            executeResource(resourcePath, null);
            return this;
        }

        public VersionBuilder executeResource(String resourcePath, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                for (String statement : readSqlFromResource(resourcePath)) {
                    statements.add(statement);
                }
            }
            return this;
        }

        public VersionBuilder executeIfInitial(String statement) {
            return executeIfInitial(statement, null);
        }

        public VersionBuilder executeIfInitial(String statement, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                initialStatements.add(statement);
            }
            return this;
        }

        public VersionBuilder executeIfInitial(Query statement) {
            return executeIfInitial(statement, null);
        }

        public VersionBuilder executeIfInitial(Query statement, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                initialStatements.add(statement);
            }
            return this;
        }

        public VersionBuilder executeResourceIfInitial(String resourcePath) {
            return executeResourceIfInitial(resourcePath, null);
        }

        public VersionBuilder executeResourceIfInitial(String resourcePath, Class<? extends SQLDialect> dialect) {
            if (dialect == null ||
                    tableManager.getDialect().getClass().equals(dialect)) {
                for (String statement : readSqlFromResource(resourcePath)) {
                    initialStatements.add(statement);
                }
            }
            return this;
        }

        public SchemaSynchronizer endVersion() {
            return synchronizer;
        }

        private List<String> readSqlFromResource(String path) {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    throw new RuntimeException("SQL resource not found: " + path);
                }

                if (tableManager.getDialect().isMultiStatementsSupported()) {
                    return List.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                } else {
                    return tableManager.getDialect()
                            .parseMultiStatements(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load SQL resource: " + path, e);
            }
        }
    }

    public class SchemaBuilder {
        private final VersionBuilder versionBuilder;
        private boolean autoCreationEnabled;
        private boolean autoMigrationEnabled;

        public SchemaBuilder(VersionBuilder versionBuilder) {
            this.versionBuilder = versionBuilder;
        }

        public SchemaBuilder enableAutoCreation(boolean enable) {
            this.autoCreationEnabled = enable;
            return this;
        }

        public SchemaBuilder enableAutoMigration(boolean enable) {
            this.autoMigrationEnabled = enable;
            return this;
        }

        public VersionBuilder endSchema() {
            return versionBuilder;
        }
    }
}
