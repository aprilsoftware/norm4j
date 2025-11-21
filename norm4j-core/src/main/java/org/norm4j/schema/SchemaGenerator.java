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
import java.util.List;

import org.norm4j.Column;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.TableGenerator;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.ForeignKeyMetadata;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.metadata.SequenceMetadata;
import org.norm4j.metadata.TableIdGenerator;
import org.norm4j.metadata.TableMetadata;

public class SchemaGenerator {
    private final MetadataManager metadataManager;

    public SchemaGenerator(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public Schema generate(String version) {
        Schema schema = new Schema();
        schema.setVersion(version);
        schema.setSchemaModelVersion(1);

        generateSequenceTables(schema);

        for (TableMetadata tableMetadata : metadataManager.getTableMetadata()) {
            for (SequenceMetadata sequenceMetadata : metadataManager.getSequenceMetadata(tableMetadata)) {
                Schema.Sequence sequence = new Schema.Sequence();

                sequence.setSchema(sequenceMetadata.getSchema());
                sequence.setName(sequenceMetadata.getName());
                sequence.setInitialValue(sequenceMetadata.getInitialValue());

                schema.getSequences().add(sequence);
            }

            schema.getTables().add(toTable(tableMetadata));
        }

        return schema;
    }

    private void generateSequenceTables(Schema schema) {
        List<String> tableGenerators = new ArrayList<>();

        for (TableMetadata tableMetadata : metadataManager.getTableMetadata()) {
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

                String generatorTableName = metadataManager.getDialect().getTableName(idGenerator.getSchema(),
                        idGenerator.getTable());

                if (tableGenerators.contains(generatorTableName)) {
                    continue;
                }

                Schema.Table table;

                table = new Schema.Table();

                table.setSchema(idGenerator.getSchema());
                table.setName(idGenerator.getTable());

                Schema.Column pkColumn;

                pkColumn = new Schema.Column();

                pkColumn.setName(idGenerator.getPkColumnName());
                pkColumn.setType(String.class.getTypeName());

                table.getColumns().add(pkColumn);

                Schema.Column valueColumn;

                valueColumn = new Schema.Column();

                valueColumn.setName(idGenerator.getValueColumnName());
                valueColumn.setType(Long.class.getTypeName());

                table.getColumns().add(valueColumn);

                Schema.PrimaryKey primaryKey;

                primaryKey = new Schema.PrimaryKey();

                primaryKey.getColumns().add(pkColumn.getName());

                table.setPrimaryKey(primaryKey);

                schema.getTables().add(table);

                tableGenerators.add(generatorTableName);
            }
        }
    }

    private Schema.Table toTable(TableMetadata tableMetadata) {
        Schema.Table table = new Schema.Table();
        table.setSchema(tableMetadata.getSchema());
        table.setName(tableMetadata.getTableName());

        List<ColumnMetadata> primaryKeyColumns = new ArrayList<>();
        for (ColumnMetadata columnMetadata : tableMetadata.getColumns()) {
            if (columnMetadata.isPrimaryKey()) {
                primaryKeyColumns.add(columnMetadata);
            }

            table.getColumns().add(toColumn(columnMetadata));
        }

        if (!primaryKeyColumns.isEmpty()) {
            Schema.PrimaryKey primaryKey;

            primaryKey = new Schema.PrimaryKey();

            for (ColumnMetadata columnMetadata : primaryKeyColumns) {
                primaryKey.getColumns().add(columnMetadata.getColumnName());
            }

            table.setPrimaryKey(primaryKey);
        }

        for (ForeignKeyMetadata foreignKeyMetadata : metadataManager.getForeignKeyMetadata(tableMetadata)) {
            Schema.ForeignKey foreignKey = new Schema.ForeignKey();

            foreignKey.setName(foreignKeyMetadata.getForeignKeyName());
            foreignKey.setReferenceSchema(foreignKeyMetadata.getReferenceTable().getSchema());
            foreignKey.setReferenceTable(foreignKeyMetadata.getReferenceTable().getTableName());
            foreignKey.setCascadeDelete(foreignKeyMetadata.getJoin().cascadeDelete());

            for (String column : foreignKeyMetadata.getJoin().columns()) {
                foreignKey.getColumns().add(column);
            }

            for (String column : foreignKeyMetadata.getJoin().reference().columns()) {
                foreignKey.getReferenceColumns().add(column);
            }

            table.getForeignKeys().add(foreignKey);
        }

        return table;
    }

    private Schema.Column toColumn(ColumnMetadata columnMetadata) {
        Schema.Column column = new Schema.Column();
        column.setName(columnMetadata.getColumnName());
        // TODO What about enum? What about any type transformation done at the dialect
        // level?
        column.setType(columnMetadata.getField().getType().getTypeName());
        column.setSequenceName(metadataManager.getDialect().getSequenceName(columnMetadata));

        GeneratedValue generatedValueAnnotation;
        Column columnAnnotation;

        columnAnnotation = (Column) columnMetadata.getAnnotations().get(Column.class);

        if (columnAnnotation != null &&
                !columnAnnotation.columnDefinition().isEmpty()) {
            column.setColumnDefinition(columnAnnotation.columnDefinition());
            column.setLength(columnAnnotation.length());
        }

        generatedValueAnnotation = (GeneratedValue) columnMetadata.getAnnotations()
                .get(GeneratedValue.class);

        if (generatedValueAnnotation == null) {
            if (columnMetadata.getField().getType().isPrimitive() ||
                    (columnAnnotation != null && !columnAnnotation.nullable()) ||
                    columnMetadata.isPrimaryKey()) {
                column.setNullable(false);
            } else {
                column.setNullable(true);
            }
        } else {
            column.setGenerationStrategy(generatedValueAnnotation.strategy().name());
            column.setNullable(false);
        }

        return column;
    }
}
