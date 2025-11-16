package org.norm4j.schema;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.norm4j.SelectQueryBuilder;
import org.norm4j.TableManager;
import org.norm4j.metadata.TableMetadata;

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
        TableMetadata table;

        tableManager.getMetadataManager().registerTable(SchemaVersion.class, schema, schemaVersionTable);

        table = tableManager.getMetadataManager().getMetadata(SchemaVersion.class);

        try (Connection connection = tableManager.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            tableManager.getMetadataManager().createTables(connection);

            try (PreparedStatement lockStatement = connection
                    .prepareStatement(
                            "SELECT * FROM " + tableManager.getDialect().getTableName(table) + " FOR UPDATE")) {
                lockStatement.executeQuery();

                for (VersionBuilder versionBuilder : versionBuilders) {
                    if (isAlreadyApplied(tableManager, connection, versionBuilder.name))
                        continue;

                    for (String statement : versionBuilder.initStatements) {
                        tableManager.createQuery(statement).executeUpdate(connection);
                    }

                    for (Class<?> tableClass : versionBuilder.tableClasses) {
                        tableManager.getMetadataManager().registerTable(tableClass);
                    }

                    tableManager.getMetadataManager().createTables(connection);

                    for (String statement : versionBuilder.finalizeStatements) {
                        tableManager.createQuery(statement).executeUpdate(connection);
                    }

                    try (PreparedStatement insertStatement = connection.prepareStatement(
                            "INSERT INTO "
                                    + tableManager.getDialect().getTableName(table)
                                    + " (name, description, creationdate)"
                                    + " VALUES (?, ?, ?)")) {
                        insertStatement.setString(1, versionBuilder.name);
                        insertStatement.setString(2, versionBuilder.description);
                        insertStatement.setDate(3, new Date(System.currentTimeMillis()));
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
        private final List<String> initStatements;
        private final List<Class<?>> tableClasses;
        private final List<String> finalizeStatements;
        private String name;
        private String description;

        public VersionBuilder(SchemaSynchronizer synchronizer) {
            this.synchronizer = synchronizer;

            tableClasses = new ArrayList<>();

            initStatements = new ArrayList<>();

            finalizeStatements = new ArrayList<>();
        }

        public VersionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VersionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public VersionBuilder table(Class<?> tableClass) {
            tableClasses.add(tableClass);
            return this;
        }

        public VersionBuilder tables(Class<?>... tableClasses) {
            this.tableClasses.addAll(Arrays.asList(tableClasses));
            return this;
        }

        public VersionBuilder tables(List<Class<?>> tableClasses) {
            this.tableClasses.addAll(tableClasses);
            return this;
        }

        public VersionBuilder init(String statement) {
            if (statement != null) {
                initStatements.add(statement);
            }
            return this;
        }

        public VersionBuilder finalize(String statement) {
            if (statement != null) {
                finalizeStatements.add(statement);
            }
            return this;
        }

        public SchemaSynchronizer end() {
            return synchronizer;
        }
    }
}
