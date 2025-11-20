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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Schema {
    private String version;
    private int schemaModelVersion;
    private List<Table> tables;
    private List<Sequence> sequences;

    public Schema() {
        tables = new ArrayList<>();
        sequences = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSchemaModelVersion() {
        return schemaModelVersion;
    }

    public void setSchemaModelVersion(int schemaModelVersion) {
        this.schemaModelVersion = schemaModelVersion;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<Sequence> sequences) {
        this.sequences = sequences;
    }

    public static Schema loadFromResource(String resourcePath) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new RuntimeException("Schema not found: " + resourcePath);
            }

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(is, Schema.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema from: " + resourcePath, e);
        }
    }

    public void write(Path path) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (OutputStream os = Files.newOutputStream(path)) {
            mapper.writeValue(os, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write schema: " + path.toString(), e);
        }
    }

    public static class Table {
        private String schema;
        private String name;
        private List<Column> columns;
        private PrimaryKey primaryKey;
        private List<ForeignKey> foreignKeys;

        public Table() {
            columns = new ArrayList<>();
            foreignKeys = new ArrayList<>();
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Column> getColumns() {
            return columns;
        }

        public void setColumns(List<Column> columns) {
            this.columns = columns;
        }

        public PrimaryKey getPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(PrimaryKey primaryKey) {
            this.primaryKey = primaryKey;
        }

        public List<ForeignKey> getForeignKeys() {
            return foreignKeys;
        }

        public void setForeignKeys(List<ForeignKey> foreignKeys) {
            this.foreignKeys = foreignKeys;
        }
    }

    public static class Column {
        private String name;
        private String type;
        private boolean nullable;
        private String columnDefinition;
        private int length;
        private String generationStrategy;
        private String sequenceName;

        public Column() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public String getColumnDefinition() {
            return columnDefinition;
        }

        public void setColumnDefinition(String columnDefinition) {
            this.columnDefinition = columnDefinition;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getGenerationStrategy() {
            return generationStrategy;
        }

        public void setGenerationStrategy(String generationStrategy) {
            this.generationStrategy = generationStrategy;
        }

        public String getSequenceName() {
            return sequenceName;
        }

        public void setSequenceName(String sequenceName) {
            this.sequenceName = sequenceName;
        }
    }

    public static class PrimaryKey {
        private List<String> columns;

        public PrimaryKey() {
            columns = new ArrayList<>();
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }
    }

    public static class ForeignKey {
        private String name;
        private List<String> columns;
        private String referenceSchema;
        private String referenceTable;
        private List<String> referenceColumns;
        private boolean cascadeDelete;

        public ForeignKey() {
            columns = new ArrayList<>();

            referenceColumns = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public String getReferenceSchema() {
            return referenceSchema;
        }

        public void setReferenceSchema(String referenceSchema) {
            this.referenceSchema = referenceSchema;
        }

        public String getReferenceTable() {
            return referenceTable;
        }

        public void setReferenceTable(String referenceTable) {
            this.referenceTable = referenceTable;
        }

        public List<String> getReferenceColumns() {
            return referenceColumns;
        }

        public void setReferenceColumns(List<String> referenceColumns) {
            this.referenceColumns = referenceColumns;
        }

        public boolean isCascadeDelete() {
            return cascadeDelete;
        }

        public void setCascadeDelete(boolean cascadeDelete) {
            this.cascadeDelete = cascadeDelete;
        }
    }

    public static class Sequence {
        private String schema;
        private String name;
        private int initialValue;

        public Sequence() {
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getInitialValue() {
            return initialValue;
        }

        public void setInitialValue(int initialValue) {
            this.initialValue = initialValue;
        }
    }
}
