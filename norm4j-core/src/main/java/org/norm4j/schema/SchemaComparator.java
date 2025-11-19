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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.norm4j.schema.migrations.AddColumnOperation;
import org.norm4j.schema.migrations.AddForeignKeyOperation;
import org.norm4j.schema.migrations.AddSequenceOperation;
import org.norm4j.schema.migrations.AddTableOperation;
import org.norm4j.schema.migrations.MigrationOperation;

public class SchemaComparator {
    public SchemaComparator() {
    }

    public static List<MigrationOperation> compare(Schema from, Schema to) {
        List<MigrationOperation> operations = new ArrayList<>();

        Map<String, Schema.Table> fromTables = indexTables(from);
        Map<String, Schema.Table> toTables = indexTables(to);

        for (Schema.Table toTable : toTables.values()) {
            String key = key(toTable.getSchema(), toTable.getName());
            Schema.Table fromTable = fromTables.get(key);

            if (fromTable == null) {
                operations.add(new AddTableOperation(toTable));
            } else {
                compareTable(fromTable, toTable, operations);
            }
        }

        Map<String, Schema.Sequence> fromSequences = indexSequences(from);
        Map<String, Schema.Sequence> toSequences = indexSequences(to);

        for (Schema.Sequence sequence : toSequences.values()) {
            String key = key(sequence.getSchema(), sequence.getName());
            if (!fromSequences.containsKey(key)) {
                operations.add(new AddSequenceOperation(sequence));
            }
        }

        return operations;
    }

    private static Map<String, Schema.Table> indexTables(Schema schema) {
        if (schema.getTables() == null) {
            return Collections.emptyMap();
        }
        return schema.getTables().stream()
                .collect(Collectors.toMap(
                        t -> key(t.getSchema(), t.getName()),
                        t -> t));
    }

    private static Map<String, Schema.Sequence> indexSequences(Schema schema) {
        if (schema.getSequences() == null) {
            return Collections.emptyMap();
        }
        return schema.getSequences().stream()
                .collect(Collectors.toMap(
                        s -> key(s.getSchema(), s.getName()),
                        s -> s));
    }

    private static String key(String schema, String name) {
        if (schema == null || schema.isEmpty()) {
            return name.toLowerCase(Locale.ROOT);
        }
        return (schema + "." + name).toLowerCase(Locale.ROOT);
    }

    private static void compareTable(Schema.Table from,
            Schema.Table to,
            List<MigrationOperation> operations) {

        Map<String, Schema.Column> fromColumns = from.getColumns() == null
                ? Collections.emptyMap()
                : from.getColumns().stream()
                        .collect(Collectors.toMap(
                                c -> c.getName().toLowerCase(Locale.ROOT),
                                c -> c));

        Map<String, Schema.Column> toColumns = to.getColumns() == null
                ? Collections.emptyMap()
                : to.getColumns().stream()
                        .collect(Collectors.toMap(
                                c -> c.getName().toLowerCase(Locale.ROOT),
                                c -> c));

        for (Schema.Column column : toColumns.values()) {
            if (!fromColumns.containsKey(column.getName().toLowerCase(Locale.ROOT))) {
                if (column.isNullable() || column.getDefaultValue() != null) {
                    operations.add(new AddColumnOperation(to.getSchema(), to.getName(), column));
                }
            }
        }

        Map<String, Schema.ForeignKey> fromForeignKeys = indexFks(from);
        Map<String, Schema.ForeignKey> toForeignKeys = indexFks(to);

        for (Schema.ForeignKey foreignKey : toForeignKeys.values()) {
            if (!fromForeignKeys.containsKey(foreignKey.getName().toLowerCase(Locale.ROOT))) {
                operations.add(new AddForeignKeyOperation(to.getSchema(), to.getName(), foreignKey));
            }
        }
    }

    private static Map<String, Schema.ForeignKey> indexFks(Schema.Table table) {
        if (table.getForeignKeys() == null) {
            return Collections.emptyMap();
        }
        return table.getForeignKeys().stream()
                .collect(Collectors.toMap(
                        fk -> fk.getName().toLowerCase(Locale.ROOT),
                        fk -> fk));
    }
}
