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
package org.norm4j.schema.migrations;

import org.norm4j.schema.Schema;

public class AddColumnOperation implements MigrationOperation {
    private final String tableSchema;
    private final String tableName;
    private final Schema.Column column;

    public AddColumnOperation(String tableSchema, String tableName, Schema.Column column) {
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.column = column;
    }

    public Type getType() {
        return Type.ADD_COLUMN;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public Schema.Column getColumn() {
        return column;
    }
}
