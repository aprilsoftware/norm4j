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

import org.norm4j.Join;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public class SchemaTable {
    private String className;
    private String tableName;
    private String schema;
    private List<SchemaJoin> joins;
    private List<SchemaColumn> columns;

    public SchemaTable() {
        joins = new ArrayList<>();
        columns = new ArrayList<>();
    }

    public SchemaTable(TableMetadata tableMetadata) {
        this();

        this.className = tableMetadata.getClass().getName();
        this.tableName = tableMetadata.getTableName();
        this.schema = tableMetadata.getSchema();

        for (Join join : tableMetadata.getJoins()) {
            joins.add(new SchemaJoin(join));
        }

        for (ColumnMetadata columnMetadata : tableMetadata.getColumns()) {
            columns.add(new SchemaColumn(columnMetadata));
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<SchemaJoin> getJoins() {
        return joins;
    }

    public void setJoins(List<SchemaJoin> joins) {
        this.joins = joins;
    }

    public List<SchemaColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<SchemaColumn> columns) {
        this.columns = columns;
    }
}
