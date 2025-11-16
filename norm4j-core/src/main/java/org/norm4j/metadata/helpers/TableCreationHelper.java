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
import java.util.HashMap;
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
                        (TableGenerator) column.getAnnotations().get(TableGenerator.class));

                String generatorTableName = dialect.getTableName(idGenerator.getSchema(), idGenerator.getTable());

                if (existingTableGenerators.contains(generatorTableName)) {
                    continue;
                }

                if (!dialect.tableExists(connection, idGenerator.getSchema(), idGenerator.getTable())) {
                    String sql = dialect.createSequenceTable(
                            idGenerator.getSchema(),
                            idGenerator.getTable(),
                            idGenerator.getPkColumnName(),
                            idGenerator.getValueColumnName());
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
                        sequenceMetadata.initialValue));
            }
        }
    }

    private SequenceMetadata getSequenceMetadataInfo(ColumnMetadata column, SQLDialect dialect,
            TableMetadata tableMetadata) {
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
            Map<String, List<Join>> foreignKeyMap = new HashMap<>();
            List<Join> namedForeignKeys = new ArrayList<>();

            if (tableExists(tableMetadata, existingTables)) {
                continue;
            }

            for (Join join : entry.getValue().getJoins()) {
                String foreignKeyName;

                if (!join.referencialIntegrity()) {
                    continue;
                }

                if (join.name().isEmpty()) {
                    foreignKeyName = dialect.createForeignKeyName(tableMetadata,
                            metadataMap.get(join.reference().table()),
                            join);
                } else {
                    namedForeignKeys.add(join);

                    continue;
                }

                if (foreignKeyMap.containsKey(foreignKeyName)) {
                    foreignKeyMap.get(foreignKeyName).add(join);
                } else {
                    List<Join> foreignKeys = new ArrayList<>();

                    foreignKeys.add(join);

                    foreignKeyMap.put(foreignKeyName, foreignKeys);
                }
            }

            for (Join foreignKey : namedForeignKeys) {
                if (foreignKeyMap.containsKey(foreignKey.name())) {
                    throw new RuntimeException("More than one join with the same name"
                            + foreignKey.name());
                } else {
                    sqlExecutor.execute(connection, dialect.alterTable(
                            tableMetadata,
                            metadataMap.get(foreignKey.reference().table()),
                            foreignKey,
                            foreignKey.name()));
                }
            }

            for (String foreignKeyName : foreignKeyMap.keySet()) {
                List<Join> foreignKeys = foreignKeyMap.get(foreignKeyName);

                if (foreignKeys.size() == 1) {
                    Join join = foreignKeys.get(0);

                    sqlExecutor.execute(connection, dialect.alterTable(
                            tableMetadata,
                            metadataMap.get(join.reference().table()),
                            join,
                            foreignKeyName));
                } else {
                    for (int i = 0; i < foreignKeys.size(); i++) {
                        Join join = foreignKeys.get(i);

                        sqlExecutor.execute(connection, dialect.alterTable(
                                tableMetadata,
                                metadataMap.get(join.reference().table()),
                                join,
                                foreignKeyName + "_" + (i + 1)));
                    }
                }
            }
        }
    }

    public static void validateJoins(Join[] joins) {
        for (Join join : joins) {
            if (join.columns().length == 0 || join.reference().columns().length == 0) {
                throw new IllegalArgumentException("Missing column(s) in join.");
            }
            if (join.columns().length != join.reference().columns().length) {
                throw new IllegalArgumentException("Mismatched number of columns in join.");
            }
        }
    }

    public static String decapitalize(String name) {
        if (name == null || name.isEmpty())
            return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private record SequenceMetadata(String schema,
            String name,
            int initialValue) {
    }
}
