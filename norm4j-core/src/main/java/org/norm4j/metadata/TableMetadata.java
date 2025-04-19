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
package org.norm4j.metadata;

import java.util.ArrayList;
import java.util.List;

import org.norm4j.Join;

public class TableMetadata
{
    private final Class<?> tableClass;
    private final String tableName;
    private final String schema;
    private final Class<?> idClass;
    private final Join[] joins;
    private final List<ColumnMetadata> columns;

    public TableMetadata(Class<?> tableClass,
            String tableName,
            String schema,
            Class<?> idClass,
            Join[] joins)
    {
        this.tableClass = tableClass;

        this.tableName = tableName;

        this.schema = schema;

        this.idClass = idClass;

        this.joins = joins;

        this.columns = new ArrayList<>();
    }

    public Class<?> getTableClass()
    {
        return tableClass;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getSchema()
    {
        return schema;
    }

    public Class<?> getIdClass()
    {
        return idClass;
    }

    public Join[] getJoins()
    {
        return joins;
    }

    public List<ColumnMetadata> getColumns()
    {
        return columns;
    }

    public List<ColumnMetadata> getPrimaryKeys()
    {
        List<ColumnMetadata> primaryKeys;

        primaryKeys = new ArrayList<>();

        for (ColumnMetadata column : columns)
        {
            if (column.isPrimaryKey())
            {
                primaryKeys.add(column);
            }
        }

        return primaryKeys;
    }
}
