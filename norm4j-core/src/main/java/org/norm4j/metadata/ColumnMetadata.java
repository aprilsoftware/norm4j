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

import org.norm4j.Column;
import org.norm4j.Id;

import java.lang.reflect.Field;
import java.util.Map;

public class ColumnMetadata
{
    private final TableMetadata table;
    private final Map<Class<?>, Object> annotations;
    private final Field field;

    public ColumnMetadata(TableMetadata table, Map<Class<?>, Object> annotations, Field field)
    {
        this.table = table;

        this.annotations = annotations;

        this.field = field;
    }

    public TableMetadata getTable()
    {
        return table;
    }

    public Map<Class<?>, Object> getAnnotations()
    {
        return annotations;
    }

    public Field getField()
    {
        return field;
    }

    public String getColumnName()
    {
        Column column;

        column = (Column)annotations.get(Column.class);

        if (column == null ||
            column.name().isEmpty())
        {
            return field.getName();
        }
        else
        {
            return column.name();
        }
    }

    public boolean isPrimaryKey()
    {
        return annotations.containsKey(Id.class);
    }
}
