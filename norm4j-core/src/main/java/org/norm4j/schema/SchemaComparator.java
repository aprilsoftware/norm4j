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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.norm4j.GenerationType;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.schema.annotations.Annotation;
import org.norm4j.schema.annotations.ColumnAnnotation;
import org.norm4j.schema.annotations.GeneratedValueAnnotation;
import org.norm4j.schema.annotations.SequenceGeneratorAnnotation;
import org.norm4j.schema.annotations.TableGeneratorAnnotation;
import org.norm4j.schema.migrations.AddColumnOperation;
import org.norm4j.schema.migrations.AddJoinOperation;
import org.norm4j.schema.migrations.AddSequenceOperation;
import org.norm4j.schema.migrations.AddTableGeneratorOperation;
import org.norm4j.schema.migrations.AddTableOperation;
import org.norm4j.schema.migrations.MigrationOperation;

public class SchemaComparator {
    public SchemaComparator() {
    }

    public List<String> generateDdl(Schema from, Schema to, SQLDialect dialect) {
        List<String> ddl = new ArrayList<>();
        List<MigrationOperation> orderedOperations;

        orderedOperations = orderOperations(compare(from, to, dialect));

        for (MigrationOperation operation : orderedOperations) {
            if (operation instanceof AddSequenceOperation aso) {
                ddl.add(dialect.createSequence(aso.getSchema(), aso.getName(), aso.getInitialValue()));
            } else if (operation instanceof AddTableGeneratorOperation atgo) {
                ddl.add(dialect.createSequenceTable(atgo.getGenerator().getSchema(),
                        atgo.getGenerator().getTable(), atgo.getGenerator().getPkColumnName(),
                        atgo.getGenerator().getValueColumnName()));
            } else if (operation instanceof AddTableOperation ato) {
                ddl.add(dialect.createTable(ato.getTable()));
            } else if (operation instanceof AddColumnOperation aco) {
                ddl.add(dialect.alterTableAddColumn(aco.getTable(), aco.getColumn()));
            } else if (operation instanceof AddJoinOperation ajo) {
                ddl.add(dialect.alterTableAddForeignKey(ajo.getTable(), ajo.getJoin(), ajo.getForeignKeyName(),
                        ajo.getReferenceTable()));
            } else {
                throw new RuntimeException("Unsupported migration operation: " +
                        operation.getClass());
            }
        }

        return ddl;
    }

    private List<MigrationOperation> orderOperations(List<MigrationOperation> operations) {
        List<MigrationOperation> sequences = new ArrayList<>();
        List<MigrationOperation> tables = new ArrayList<>();
        List<MigrationOperation> columns = new ArrayList<>();
        List<MigrationOperation> joins = new ArrayList<>();
        List<MigrationOperation> tableGenerators = new ArrayList<>();

        for (MigrationOperation operation : operations) {
            if (operation instanceof AddSequenceOperation)
                sequences.add(operation);
            else if (operation instanceof AddTableGeneratorOperation)
                tableGenerators.add(operation);
            else if (operation instanceof AddTableOperation)
                tables.add(operation);
            else if (operation instanceof AddColumnOperation)
                columns.add(operation);
            else if (operation instanceof AddJoinOperation)
                joins.add(operation);
            else
                tables.add(operation);
        }

        List<MigrationOperation> ordered = new ArrayList<>();
        ordered.addAll(sequences);
        ordered.addAll(tableGenerators);
        ordered.addAll(tables);
        ordered.addAll(columns);
        ordered.addAll(joins);

        return ordered;
    }

    public List<MigrationOperation> compare(Schema from, Schema to, SQLDialect dialect) {
        List<MigrationOperation> operations = new ArrayList<>();

        Map<String, SchemaTable> fromTables;
        Map<String, SchemaTable> fromTablesByClass;

        if (from == null) {
            fromTables = new HashMap<>();
            fromTablesByClass = new HashMap<>();
        } else {
            fromTables = indexTables(from);
            fromTablesByClass = indexTablesByClass(from);
        }

        Map<String, SchemaTable> toTables = indexTables(to);
        Map<String, SchemaTable> toTablesByClass = indexTablesByClass(to);

        for (SchemaTable toTable : toTables.values()) {
            String key = tableKey(toTable);
            SchemaTable fromTable = fromTables.get(key);

            if (fromTable == null) {
                operations.add(new AddTableOperation(toTable));

                Map<String, SchemaJoin> toForeignKeys = createForeignKeyMap(toTable, toTablesByClass, dialect);

                for (String foreignKeyName : toForeignKeys.keySet()) {
                    SchemaJoin join = toForeignKeys.get(foreignKeyName);
                    operations.add(new AddJoinOperation(toTable,
                            join,
                            foreignKeyName,
                            toTablesByClass.get(join.getReference().getTable()).getTableName()));
                }
            } else {
                compareTable(fromTable, toTable, operations, fromTablesByClass, toTablesByClass, dialect);
            }
        }

        Map<String, AddSequenceOperation> fromSequences;

        if (from == null) {
            fromSequences = new HashMap<>();
        } else {
            fromSequences = getSequences(from);
        }

        Map<String, AddSequenceOperation> toSequences = getSequences(to);

        for (var entry : toSequences.entrySet()) {
            if (!fromSequences.containsKey(entry.getKey())) {
                operations.add(entry.getValue());
            }
        }

        Map<String, AddTableGeneratorOperation> fromTableGenerators;

        if (from == null) {
            fromTableGenerators = new HashMap<>();
        } else {
            fromTableGenerators = getTableGenerators(from);
        }

        Map<String, AddTableGeneratorOperation> toTableGenerators = getTableGenerators(to);

        for (var entry : toTableGenerators.entrySet()) {
            if (!fromTableGenerators.containsKey(entry.getKey())) {
                operations.add(entry.getValue());
            }
        }

        return operations;
    }

    private Map<String, SchemaTable> indexTablesByClass(Schema schema) {
        if (schema == null || schema.getTables() == null)
            return Collections.emptyMap();

        return schema.getTables().stream()
                .collect(Collectors.toMap(
                        t -> t.getClassName(),
                        t -> t,
                        (a, b) -> a,
                        HashMap::new));
    }

    private Map<String, SchemaTable> indexTables(Schema schema) {
        return schema.getTables().stream()
                .collect(Collectors.toMap(
                        this::tableKey,
                        t -> t,
                        (a, b) -> a,
                        HashMap::new));
    }

    private String tableKey(SchemaTable table) {
        if (table.getSchema() == null || table.getSchema().isEmpty()) {
            return table.getTableName().toLowerCase();
        }

        return (table.getSchema() + "." + table.getTableName()).toLowerCase();
    }

    private void compareTable(SchemaTable fromTable,
            SchemaTable toTable,
            List<MigrationOperation> operations,
            Map<String, SchemaTable> fromTablesByClass,
            Map<String, SchemaTable> toTablesByClass,
            SQLDialect dialect) {
        Map<String, SchemaColumn> fromColumns = indexColumns(fromTable);
        Map<String, SchemaColumn> toColumns = indexColumns(toTable);

        for (SchemaColumn toColumn : toColumns.values()) {
            String columnKey = columnKey(toColumn);

            if (!fromColumns.containsKey(columnKey)) {
                if (isSafeToAdd(toColumn)) {
                    operations.add(new AddColumnOperation(toTable, toColumn));
                }
            }
        }

        Map<String, SchemaJoin> fromForeignKeys = createForeignKeyMap(fromTable, fromTablesByClass, dialect);
        Map<String, SchemaJoin> toForeignKeys = createForeignKeyMap(toTable, toTablesByClass, dialect);

        for (String foreignKeyName : toForeignKeys.keySet()) {
            if (!fromForeignKeys.containsKey(foreignKeyName)) {
                SchemaJoin join = toForeignKeys.get(foreignKeyName);
                operations.add(new AddJoinOperation(toTable,
                        join,
                        foreignKeyName,
                        toTablesByClass.get(join.getReference().getTable()).getTableName()));
            }
        }
    }

    private Map<String, SchemaColumn> indexColumns(SchemaTable table) {
        return table.getColumns().stream()
                .collect(Collectors.toMap(
                        this::columnKey,
                        c -> c,
                        (a, b) -> a,
                        HashMap::new));
    }

    private String columnKey(SchemaColumn column) {
        ColumnAnnotation columAnnotation = Annotation.get(column, ColumnAnnotation.class);

        String name = (columAnnotation != null && columAnnotation.getName() != null
                && !columAnnotation.getName().isEmpty())
                        ? columAnnotation.getName()
                        : column.getFieldName();

        return name.toLowerCase();
    }

    private boolean isSafeToAdd(SchemaColumn column) {
        ColumnAnnotation columnAnnotation = Annotation.get(column, ColumnAnnotation.class);

        return (columnAnnotation == null) || columnAnnotation.isNullable();
    }

    private Map<String, AddSequenceOperation> getSequences(Schema schema) {
        Map<String, AddSequenceOperation> sequenceMap = new HashMap<>();

        for (SchemaTable table : schema.getTables()) {
            for (SchemaColumn column : table.getColumns()) {
                GeneratedValueAnnotation generatedValueAnnotation = Annotation.get(column,
                        GeneratedValueAnnotation.class);
                if (generatedValueAnnotation == null)
                    continue;

                if (generatedValueAnnotation.getStrategy() == GenerationType.SEQUENCE) {
                    SequenceGeneratorAnnotation sequenceGeneratorAnnotation = Annotation.get(column,
                            SequenceGeneratorAnnotation.class);
                    if (sequenceGeneratorAnnotation != null) {
                        String key = sequenceKey(sequenceGeneratorAnnotation.getSchema(),
                                sequenceGeneratorAnnotation.getSequenceName());
                        sequenceMap.putIfAbsent(key,
                                new AddSequenceOperation(sequenceGeneratorAnnotation.getSchema(),
                                        sequenceGeneratorAnnotation.getSequenceName(),
                                        sequenceGeneratorAnnotation.getInitialValue()));
                    }
                }
            }
        }

        return sequenceMap;
    }

    private String sequenceKey(String schema, String name) {
        if (schema == null || schema.isEmpty()) {
            return name.toLowerCase();
        }

        return (schema + "." + name).toLowerCase();
    }

    private Map<String, AddTableGeneratorOperation> getTableGenerators(Schema schema) {
        Map<String, AddTableGeneratorOperation> tableGeneratorMap = new HashMap<>();

        for (SchemaTable table : schema.getTables()) {
            if (table.getColumns() == null)
                continue;

            for (SchemaColumn column : table.getColumns()) {
                GeneratedValueAnnotation generatedValueAnnotation = Annotation.get(column,
                        GeneratedValueAnnotation.class);
                if (generatedValueAnnotation == null || generatedValueAnnotation.getStrategy() != GenerationType.TABLE)
                    continue;

                TableGeneratorAnnotation tableGeneratorAnnotation = Annotation.get(column,
                        TableGeneratorAnnotation.class);
                if (tableGeneratorAnnotation == null)
                    continue;

                String key = tableGeneratorKey(tableGeneratorAnnotation.getSchema(),
                        tableGeneratorAnnotation.getTable());

                tableGeneratorMap.putIfAbsent(key, new AddTableGeneratorOperation(tableGeneratorAnnotation));
            }
        }

        return tableGeneratorMap;
    }

    private String tableGeneratorKey(String schema, String tableName) {
        if (schema == null || schema.isEmpty()) {
            return tableName.toLowerCase();
        }

        return (schema + "." + tableName).toLowerCase();
    }

    private Map<String, SchemaJoin> createForeignKeyMap(SchemaTable table,
            Map<String, SchemaTable> tablesByClass,
            SQLDialect dialect) {
        Map<String, List<SchemaJoin>> joinMap = new HashMap<>();
        List<SchemaJoin> namedJoins = new ArrayList<>();
        Map<String, SchemaJoin> foreignKeys = new HashMap<>();

        for (SchemaJoin join : table.getJoins()) {
            String foreignKeyName;

            if (!join.isReferencialIntegrity()) {
                continue;
            }

            if (join.getName() == null || join.getName().isEmpty()) {
                SchemaReference reference = join.getReference();
                SchemaTable referenceTable = tablesByClass.get(reference.getTable());

                foreignKeyName = dialect.createForeignKeyName(
                        table,
                        referenceTable,
                        join);
            } else {
                namedJoins.add(join);
                continue;
            }

            if (joinMap.containsKey(foreignKeyName)) {
                joinMap.get(foreignKeyName).add(join);
            } else {
                List<SchemaJoin> joins = new ArrayList<>();

                joins.add(join);

                joinMap.put(foreignKeyName, joins);
            }
        }

        for (SchemaJoin join : namedJoins) {
            if (joinMap.containsKey(join.getName())) {
                throw new RuntimeException("More than one join with the same name"
                        + join.getName());
            } else {
                foreignKeys.put(join.getName().toLowerCase(), join);
            }
        }

        for (String foreignKeyName : joinMap.keySet()) {
            List<SchemaJoin> joins = joinMap.get(foreignKeyName);

            if (joins.size() == 1) {
                SchemaJoin join = joins.get(0);
                join.setName(foreignKeyName);
                foreignKeys.put(foreignKeyName.toLowerCase(), join);
            } else {
                for (int i = 0; i < joins.size(); i++) {
                    SchemaJoin join = joins.get(i);
                    join.setName(foreignKeyName + "_" + (i + 1));
                    foreignKeys.put(join.getName().toLowerCase(), join);
                }
            }
        }

        return foreignKeys;
    }
}
