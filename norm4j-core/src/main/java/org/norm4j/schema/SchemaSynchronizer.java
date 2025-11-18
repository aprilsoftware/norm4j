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
import java.util.ArrayList;
import java.util.List;

import org.norm4j.Query;
import org.norm4j.SelectQueryBuilder;
import org.norm4j.TableManager;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;

public class SchemaSynchronizer {
    private final List<VersionBuilder> versionBuilders;
    private final TableManager tableManager;
    private String schemaVersionTable;
    private String schema;

    public SchemaSynchronizer(TableManager tableManager) {
        this.tableManager = tableManager;

        versionBuilders = new ArrayList<>();
    }

    public SchemaSynchronizer schemaVersionTable(String schemaVersionTable) {
        this.schemaVersionTable = schemaVersionTable;
        return this;
    }

    public SchemaSynchronizer schema(String schemaName) {
        this.schema = schemaName;
        return this;
    }

    public VersionBuilder version() {
        VersionBuilder builder;

        builder = new VersionBuilder(this);

        versionBuilders.add(builder);

        return builder;
    }

    public SchemaSynchronizer apply() {
        ColumnMetadata column;

        tableManager.getMetadataManager().registerTable(SchemaVersion.class, schema, schemaVersionTable);

        column = tableManager.getMetadataManager().getMetadata(SchemaVersion.class, "creationDate");

        try (Connection connection = tableManager.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            if (!tableManager.getMetadataManager().getDialect()
                    .tableExists(connection, column.getTable().getSchema(),
                            column.getTable().getTableName())) {
                executeQuery(tableManager,
                        connection,
                        tableManager.getDialect().createTable(column.getTable()));
            }

            try (PreparedStatement lockStatement = tableManager.getDialect()
                    .createLockStatement(connection, column.getTable())) {
                lockStatement.executeQuery();

                for (VersionBuilder versionBuilder : versionBuilders) {
                    if (isAlreadyApplied(tableManager, connection, versionBuilder.name))
                        continue;

                    for (Object statement : versionBuilder.statements) {
                        executeQuery(tableManager, connection, statement);
                    }

                    try (PreparedStatement insertStatement = connection.prepareStatement(
                            "INSERT INTO "
                                    + tableManager.getDialect().getTableName(column.getTable())
                                    + " (name, description, creationdate)"
                                    + " VALUES (?, ?, ?)")) {
                        insertStatement.setString(1, versionBuilder.name);
                        insertStatement.setString(2, versionBuilder.description);
                        insertStatement.setObject(3,
                                tableManager.getDialect().toSqlValue(column,
                                        new Date(System.currentTimeMillis())));
                        insertStatement.executeUpdate();
                    }
                }
            }

            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        versionBuilders.clear();

        return this;
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

    public class VersionBuilder {
        private final SchemaSynchronizer synchronizer;
        private final List<Object> statements;
        private String name;
        private String description;

        public VersionBuilder(SchemaSynchronizer synchronizer) {
            this.synchronizer = synchronizer;

            statements = new ArrayList<>();
        }

        public VersionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VersionBuilder description(String description) {
            this.description = description;
            return this;
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
}
