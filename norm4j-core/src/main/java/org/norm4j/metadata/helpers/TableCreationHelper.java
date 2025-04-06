package org.norm4j.metadata.helpers;

import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Join;
import org.norm4j.SequenceGenerator;
import org.norm4j.TableGenerator;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableIdGenerator;
import org.norm4j.metadata.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableCreationHelper {

    private final Map<Class<?>, TableMetadata> metadataMap;
    private final SqlExecutor sqlExecutor;

    public TableCreationHelper(Map<Class<?>, TableMetadata> metadataMap, SqlExecutor sqlExecutor) {
        this.metadataMap = metadataMap;
        this.sqlExecutor = sqlExecutor;
    }

    public void createSequenceTables(Connection connection, SQLDialect dialect) throws SQLException {
        List<String> existingTableGenerators = new ArrayList<>();

        for (TableMetadata tableMetadata : metadataMap.values()) {
            for (ColumnMetadata column : tableMetadata.getColumns()) {
                if (!column.getAnnotations().containsKey(GeneratedValue.class)) {
                    continue;
                }

                GeneratedValue generatedValue = (GeneratedValue) column.getAnnotations().get(GeneratedValue.class);
                if (generatedValue.strategy() != GenerationType.TABLE) {
                    continue;
                }

                TableIdGenerator idGenerator = new TableIdGenerator(
                        (TableGenerator) column.getAnnotations().get(TableGenerator.class)
                );

                String generatorTableName = dialect.getTableName(idGenerator.getSchema(), idGenerator.getTable());

                if (existingTableGenerators.contains(generatorTableName)) {
                    continue;
                }

                if (!dialect.tableExists(connection, idGenerator.getSchema(), idGenerator.getTable())) {
                    String sql = dialect.createSequenceTable(
                            idGenerator.getSchema(),
                            idGenerator.getTable(),
                            idGenerator.getPkColumnName(),
                            idGenerator.getValueColumnName()
                    );
                    sqlExecutor.execute(connection, sql);
                }

                existingTableGenerators.add(generatorTableName);
            }
        }

    }

    public List<TableMetadata> getExistingTables(Connection connection, SQLDialect dialect) throws SQLException {
        List<TableMetadata> existingTables = new ArrayList<>();

        for (TableMetadata tableMetadata : metadataMap.values()) {
            if (dialect.tableExists(connection, tableMetadata.getSchema(), tableMetadata.getTableName())) {
                existingTables.add(tableMetadata);
            }
        }

        return existingTables;
    }

    public void createTablesAndSequences(Connection connection, SQLDialect dialect,
                                         List<TableMetadata> existingTables) throws SQLException {
        for (TableMetadata tableMetadata : metadataMap.values()) {
            if (tableExists(tableMetadata, existingTables)) {
                continue;
            }

            createSequencesForTable(connection, dialect, tableMetadata);
            sqlExecutor.execute(connection, dialect.createTable(tableMetadata));
        }
    }

    public boolean tableExists(TableMetadata tableMetadata, List<TableMetadata> existingTables) {
        return existingTables.stream()
                .anyMatch(t -> t.getSchema().equals(tableMetadata.getSchema()) &&
                        t.getTableName().equals(tableMetadata.getTableName()));
    }

    public void createSequencesForTable(Connection connection, SQLDialect dialect,
                                        TableMetadata tableMetadata) throws SQLException {
        for (ColumnMetadata column : tableMetadata.getColumns()) {
            if (!column.getAnnotations().containsKey(GeneratedValue.class)) {
                continue;
            }

            GeneratedValue generatedValue = (GeneratedValue) column.getAnnotations().get(GeneratedValue.class);
            if (generatedValue.strategy() != GenerationType.SEQUENCE) {
                continue;
            }

            SequenceMetadata sequenceMetadata = getSequenceMetadataInfo(column, dialect, tableMetadata);

            if (!dialect.sequenceExists(connection, sequenceMetadata.schema, sequenceMetadata.name)) {
                sqlExecutor.execute(connection, dialect.createSequence(
                        sequenceMetadata.schema,
                        sequenceMetadata.name,
                        sequenceMetadata.initialValue
                ));
            }
        }
    }

    private SequenceMetadata getSequenceMetadataInfo(ColumnMetadata column, SQLDialect dialect, TableMetadata tableMetadata) {
        String schema = "";
        String sequenceName = "";
        int initialValue = 1;

        if (column.getAnnotations().containsKey(SequenceGenerator.class)) {
            SequenceGenerator sequenceGenerator = (SequenceGenerator) column.getAnnotations()
                    .get(SequenceGenerator.class);

            schema = sequenceGenerator.schema();
            sequenceName = sequenceGenerator.sequenceName();
            initialValue = sequenceGenerator.initialValue();
        }

        if (sequenceName.isEmpty()) {
            sequenceName = dialect.createSequenceName(tableMetadata, column);
        }

        return new SequenceMetadata(schema, sequenceName, initialValue);
    }

    public void addForeignKeyConstraints(Connection connection, SQLDialect dialect,
                                         List<TableMetadata> existingTables) throws SQLException {
        for (Map.Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet()) {
            TableMetadata tableMetadata = metadataMap.get(entry.getKey());

            if (tableExists(tableMetadata, existingTables)) {
                continue;
            }

            for (Join join : entry.getValue().getJoins()) {
                if (join.referencialIntegrity()) {
                    sqlExecutor.execute(connection, dialect.alterTable(
                            tableMetadata,
                            metadataMap.get(join.reference().table()),
                            join
                    ));
                }
            }
        }
    }

    // Helper record to simplify sequence information handling
    private record SequenceMetadata(String schema,
                                    String name,
                                    int initialValue) {}
}
