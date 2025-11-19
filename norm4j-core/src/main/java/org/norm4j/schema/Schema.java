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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Schema {
    private String version;
    private int schemaModelVersion;
    private List<Table> tables;
    private List<Sequence> sequences;

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

            // ObjectMapper mapper = new ObjectMapper();

            // return mapper.readValue(is, Schema.class);

            return null;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema from: " + resourcePath, e);
        }
    }

    public static class Table {
        private String schema;
        private String name;
        private List<Column> columns;
        private PrimaryKey primaryKey;
        private List<ForeignKey> foreignKeys;

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
        private String logicalType;
        private Integer length;
        private Integer precision;
        private Integer scale;
        private boolean nullable;
        private String defaultValue;
        private Identity identity;
        private boolean array;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogicalType() {
            return logicalType;
        }

        public void setLogicalType(String logicalType) {
            this.logicalType = logicalType;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public Integer getPrecision() {
            return precision;
        }

        public void setPrecision(Integer precision) {
            this.precision = precision;
        }

        public Integer getScale() {
            return scale;
        }

        public void setScale(Integer scale) {
            this.scale = scale;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Identity getIdentity() {
            return identity;
        }

        public void setIdentity(Identity identity) {
            this.identity = identity;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }
    }

    public static class Identity {
        private String strategy;
        private String sequenceName;

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getSequenceName() {
            return sequenceName;
        }

        public void setSequenceName(String sequenceName) {
            this.sequenceName = sequenceName;
        }
    }

    public static class PrimaryKey {
        private String name;
        private List<String> columns;

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
    }

    public static class ForeignKey {
        private String name;
        private List<String> columns;
        private String referencedSchema;
        private String referencedTable;
        private List<String> referencedColumns;
        private String onDelete;
        private String onUpdate;

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

        public String getReferencedSchema() {
            return referencedSchema;
        }

        public void setReferencedSchema(String referencedSchema) {
            this.referencedSchema = referencedSchema;
        }

        public String getReferencedTable() {
            return referencedTable;
        }

        public void setReferencedTable(String referencedTable) {
            this.referencedTable = referencedTable;
        }

        public List<String> getReferencedColumns() {
            return referencedColumns;
        }

        public void setReferencedColumns(List<String> referencedColumns) {
            this.referencedColumns = referencedColumns;
        }

        public String getOnDelete() {
            return onDelete;
        }

        public void setOnDelete(String onDelete) {
            this.onDelete = onDelete;
        }

        public String getOnUpdate() {
            return onUpdate;
        }

        public void setOnUpdate(String onUpdate) {
            this.onUpdate = onUpdate;
        }
    }

    public static class Sequence {
        private String schema;
        private String name;
        private long startWith;
        private long incrementBy;

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

        public long getStartWith() {
            return startWith;
        }

        public void setStartWith(long startWith) {
            this.startWith = startWith;
        }

        public long getIncrementBy() {
            return incrementBy;
        }

        public void setIncrementBy(long incrementBy) {
            this.incrementBy = incrementBy;
        }
    }
}
