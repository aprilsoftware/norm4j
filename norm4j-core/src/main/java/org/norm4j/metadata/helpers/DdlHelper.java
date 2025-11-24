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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Join;
import org.norm4j.SequenceGenerator;
import org.norm4j.TableGenerator;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.ForeignKeyMetadata;
import org.norm4j.metadata.SequenceMetadata;
import org.norm4j.metadata.TableIdGenerator;
import org.norm4j.metadata.TableMetadata;

public class DdlHelper {
    private final Map<Class<?>, TableMetadata> metadataMap;

    public DdlHelper(Map<Class<?>, TableMetadata> metadataMap) {
        this.metadataMap = metadataMap;
    }

    public boolean tableExists(TableMetadata tableMetadata, List<TableMetadata> existingTables) {
        return existingTables.stream()
                .anyMatch(t -> t.getSchema().equals(tableMetadata.getSchema()) &&
                        t.getTableName().equals(tableMetadata.getTableName()));
    }

    public List<String> createSequenceTables(SQLDialect dialect) {
        List<String> tableGenerators = new ArrayList<>();
        List<String> ddl = new ArrayList<>();

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

                if (tableGenerators.contains(generatorTableName)) {
                    continue;
                }

                String sql = dialect.createSequenceTable(
                        idGenerator.getSchema(),
                        idGenerator.getTable(),
                        idGenerator.getPkColumnName(),
                        idGenerator.getValueColumnName());

                ddl.add(sql);

                tableGenerators.add(generatorTableName);
            }
        }

        return ddl;
    }

    public List<String> createSequences(TableMetadata table, SQLDialect dialect) {
        List<String> ddl = new ArrayList<>();

        for (SequenceMetadata sequenceMetadata : getSequences(table, dialect)) {
            ddl.add(dialect.createSequence(
                    sequenceMetadata.getSchema(),
                    sequenceMetadata.getName(),
                    sequenceMetadata.getInitialValue()));
        }

        return ddl;
    }

    public List<SequenceMetadata> getSequences(TableMetadata table, SQLDialect dialect) {
        List<SequenceMetadata> sequences = new ArrayList<>();

        for (ColumnMetadata column : table.getColumns()) {
            if (!column.getAnnotations().containsKey(GeneratedValue.class)) {
                continue;
            }

            GeneratedValue generatedValue = (GeneratedValue) column.getAnnotations().get(GeneratedValue.class);
            if (generatedValue.strategy() != GenerationType.SEQUENCE) {
                continue;
            }

            sequences.add(getSequence(column, dialect));
        }

        return sequences;
    }

    private SequenceMetadata getSequence(ColumnMetadata column, SQLDialect dialect) {
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
            sequenceName = dialect.createSequenceName(column);
        }

        return new SequenceMetadata(schema, sequenceName, initialValue);
    }

    public List<String> createForeignKeys(SQLDialect dialect) {
        return createForeignKeys(dialect, null);
    }

    public List<String> createForeignKeys(SQLDialect dialect, List<TableMetadata> existingTables) {
        List<String> ddl = new ArrayList<>();

        for (ForeignKeyMetadata foreignKey : getForeignKeys(dialect, existingTables)) {
            ddl.add(dialect.alterTableAddForeignKey(foreignKey));
        }

        return ddl;
    }

    public List<ForeignKeyMetadata> getForeignKeys(SQLDialect dialect, List<TableMetadata> existingTables) {
        List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();

        for (Map.Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet()) {
            if (existingTables != null &&
                    tableExists(entry.getValue(), existingTables)) {
                continue;
            }

            foreignKeys.addAll(getForeignKeys(entry.getValue(), dialect));
        }

        return foreignKeys;
    }

    public List<ForeignKeyMetadata> getForeignKeys(TableMetadata tableMetadata, SQLDialect dialect) {
        List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();
        Map<String, List<Join>> joinMap = new HashMap<>();
        List<Join> namedJoins = new ArrayList<>();

        for (Join join : tableMetadata.getJoins()) {
            String foreignKeyName;

            if (!join.referencialIntegrity()) {
                continue;
            }

            if (join.name().isEmpty()) {
                foreignKeyName = dialect.createForeignKeyName(tableMetadata,
                        metadataMap.get(join.reference().table()),
                        join);
            } else {
                namedJoins.add(join);

                continue;
            }

            if (joinMap.containsKey(foreignKeyName)) {
                joinMap.get(foreignKeyName).add(join);
            } else {
                List<Join> joins = new ArrayList<>();

                joins.add(join);

                joinMap.put(foreignKeyName, joins);
            }
        }

        for (Join join : namedJoins) {
            if (joinMap.containsKey(join.name())) {
                throw new RuntimeException("More than one join with the same name"
                        + join.name());
            } else {
                foreignKeys.add(new ForeignKeyMetadata(
                        join.name(),
                        tableMetadata,
                        metadataMap.get(join.reference().table()),
                        join));
            }
        }

        for (String foreignKeyName : joinMap.keySet()) {
            List<Join> joins = joinMap.get(foreignKeyName);

            if (joins.size() == 1) {
                Join join = joins.get(0);

                foreignKeys.add(new ForeignKeyMetadata(
                        foreignKeyName,
                        tableMetadata,
                        metadataMap.get(join.reference().table()),
                        join));
            } else {
                for (int i = 0; i < joins.size(); i++) {
                    Join join = joins.get(i);

                    foreignKeys.add(new ForeignKeyMetadata(
                            foreignKeyName + "_" + (i + 1),
                            tableMetadata,
                            metadataMap.get(join.reference().table()),
                            join));
                }
            }
        }

        return foreignKeys;
    }
}
