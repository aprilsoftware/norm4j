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
package org.norm4j;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public class Query
{
    private final Map<Integer, Object> parameters;
    private final TableManager tableManager;
    private final String sql;

    public Query(TableManager tableManager, String sql)
    {
        this.tableManager = tableManager;

        this.sql = sql;

        parameters = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(Class<T> type)
    {
        List<Object[]> rows;

        if (tableManager.getMetadataManager()
                                .getMetadata(type) == null)
        {
            rows = getResultList();
        }
        else
        {
            rows = getResultList(new Class[] {type});
        }

        if (rows.isEmpty())
        {
            return new ArrayList<>();
        }
        else
        {
            List<T> objects;

            objects = new ArrayList<>();

            for (Object[] row : rows)
            {
                objects.add((T)row[0]);
            }

            return objects;
        }
    }

    public List<Object[]> getResultList(Class<?>... tableClasses)
    {
        try (Connection connection = tableManager.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            SQLDialect dialect;

            dialect = tableManager.getMetadataManager().getDialect(connection);

            for (Map.Entry<Integer, Object> entry : parameters.entrySet())
            {
                ps.setObject(entry.getKey(), entry.getValue());
            }

            try (ResultSet rs = ps.executeQuery())
            {
                List<Object[]> rows;
                int columnCount;

                rows = new ArrayList<>();

                columnCount = rs.getMetaData().getColumnCount();

                while (rs.next())
                {
                    List<Object> columns;
                    Object[] row;
                    int index;

                    columns = new ArrayList<>();

                    index = 1;

                    for (Class<?> tableClass : tableClasses)
                    {
                        TableMetadata table;
                        Object record;

                        table = tableManager.getMetadataManager()
                                .getMetadata(tableClass);

                        if (table == null)
                        {
                            throw new IllegalArgumentException("No metadata found for class " 
                                    + tableClass.getName());
                        }

                        record = tableClass.getDeclaredConstructor().newInstance();

                        for (int i = 0; i < table.getColumns().size(); i++)
                        {
                            Object value;

                            value = rs.getObject(i + index);

                            if (value != null)
                            {
                                ColumnMetadata column;
                                String columnName;
                                Field field;

                                columnName = rs.getMetaData().getColumnName(i + index);

                                column = table.getColumns().stream()
                                        .filter(c -> c.getColumnName().equalsIgnoreCase(columnName))
                                        .findFirst().get();

                                field = column.getField();

                                field.setAccessible(true);

                                field.set(record, dialect.fromSqlValue(column, value));
                            }
                        }

                        index += table.getColumns().size();

                        columns.add(record);
                    }

                    for (int i = index; i <= columnCount; i++)
                    {
                        columns.add(rs.getObject(i));
                    }
                    
                    row = new Object[columns.size()];

                    for (int i = 0; i < columns.size(); i++)
                    {
                        row[i] = columns.get(i);
                    }

                    rows.add(row);
                }

                return rows;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object[] getSingleResult(Class<?>... tableClasses)
    {
        List<Object[]> list;

        list = getResultList(tableClasses);

        if (list.isEmpty())
        {
            return null;
        }

        return list.get(0);
    }

    public <T> T getSingleResult(Class<T> type)
    {
        List<T> list;

        list = getResultList(type);

        if (list.isEmpty())
        {
            return null;
        }

        return list.get(0);
    }

    public int executeUpdate()
    {
        try (Connection connection = tableManager.getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            for (Map.Entry<Integer, Object> entry : parameters.entrySet())
            {
                ps.setObject(entry.getKey(), entry.getValue());
            }

            return ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Object> getParameters()
    {
        return parameters;
    }

    public Object getParameter(int index)
    {
        return parameters.get(index);
    }

    public Query setParameter(int index, Object value)
    {
        parameters.put(index, value);

        return this;
    }
}
