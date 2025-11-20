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

public class Query {
    private final Map<Integer, Object> parameters;
    private final TableManager tableManager;
    private final String sql;

    public Query(TableManager tableManager, String sql) {
        this.tableManager = tableManager;

        this.sql = sql;

        parameters = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, List<V>> mapResultList(Class<K> keyType, Class<V> valueType) {
        Map<K, List<V>> map;
        List<Object[]> rows;

        map = new HashMap<>();

        rows = getResultList(keyType, valueType);

        for (Object[] row : rows) {
            List<V> values;
            V value;
            K key;

            if (row.length != 2) {
                throw new RuntimeException("Unexpected number of columns.");
            }

            key = (K) row[0];

            if (map.containsKey(key)) {
                values = map.get(key);
            } else {
                values = new ArrayList<>();

                map.put(key, values);
            }

            value = (V) row[1];

            if (value != null) {
                values.add(value);
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> mapSingleResult(Class<K> keyType, Class<V> valueType) {
        Map<K, V> map;
        List<Object[]> rows;

        map = new HashMap<>();

        rows = getResultList(keyType, valueType);

        for (Object[] row : rows) {
            if (row.length != 2) {
                throw new RuntimeException("Unexpected number of columns.");
            }

            map.put((K) row[0], (V) row[1]);
        }

        return map;
    }

    public <T> List<T> getResultList(Class<T> type) {
        return getResultList(null, type);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> List<T> getResultList(Connection connection, Class<T> type) {
        List<Object[]> rows;

        if (tableManager.getMetadataManager()
                .getTableMetadata(type) == null) {
            if (connection == null) {
                rows = getResultList();
            } else {
                rows = getResultList(connection);
            }
        } else {
            if (connection == null) {
                rows = getResultList(new Class[] { type });
            } else {
                rows = getResultList(connection, new Class[] { type });
            }
        }

        if (rows.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<T> objects;

            objects = new ArrayList<>();

            for (Object[] row : rows) {
                T value;

                value = (T) row[0];

                if (value != null &&
                        type.isEnum()) {
                    if (value instanceof Number) {
                        T[] constants;
                        int ordinal;

                        ordinal = ((Number) value).intValue();

                        constants = type.getEnumConstants();

                        if (ordinal < 0 || ordinal >= constants.length) {
                            throw new IllegalArgumentException("Invalid ordinal ("
                                    + ordinal
                                    + ") for enum "
                                    + type.getName());
                        }

                        objects.add(constants[ordinal]);
                    } else {
                        value = (T) Enum.valueOf((Class<? extends Enum>) type, value.toString());
                    }
                }

                objects.add(value);
            }

            return objects;
        }
    }

    public List<Object[]> getResultList(Class<?>... types) {
        try (Connection connection = tableManager.getDataSource().getConnection()) {
            return getResultList(connection, types);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object[]> getResultList(Connection connection, Class<?>... types) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            SQLDialect dialect;

            dialect = tableManager.getMetadataManager().getDialect(connection);

            for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
                ps.setObject(entry.getKey(), entry.getValue());
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Object[]> rows;
                int columnCount;

                rows = new ArrayList<>();

                columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    List<Object> columns;
                    Object[] row;
                    int index;

                    columns = new ArrayList<>();

                    index = 1;

                    for (Class<?> type : types) {
                        TableMetadata table;
                        Object record;

                        table = tableManager.getMetadataManager()
                                .getTableMetadata(type);

                        if (table == null) {
                            columns.add(rs.getObject(index));

                            index++;
                        } else {
                            record = type.getDeclaredConstructor().newInstance();

                            for (int i = 0; i < table.getColumns().size(); i++) {
                                Object value;

                                value = rs.getObject(i + index);

                                if (value != null) {
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
                    }

                    for (int i = index; i <= columnCount; i++) {
                        columns.add(rs.getObject(i));
                    }

                    row = new Object[columns.size()];

                    for (int i = 0; i < columns.size(); i++) {
                        row[i] = columns.get(i);
                    }

                    rows.add(row);
                }

                return rows;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object[] getSingleResult(Class<?>... tableClasses) {
        List<Object[]> list;

        list = getResultList(tableClasses);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public Object[] getSingleResult(Connection connection, Class<?>... tableClasses) {
        List<Object[]> list;

        list = getResultList(connection, tableClasses);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public <T> T getSingleResult(Class<T> type) {
        List<T> list;

        list = getResultList(type);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public <T> T getSingleResult(Connection connection, Class<T> type) {
        List<T> list;

        list = getResultList(connection, type);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public int executeUpdate() {
        try (Connection connection = tableManager.getDataSource().getConnection()) {
            return executeUpdate(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int executeUpdate(Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
                ps.setObject(entry.getKey(), entry.getValue());
            }

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Object> getParameters() {
        return parameters;
    }

    public Object getParameter(int index) {
        return parameters.get(index);
    }

    public Query setParameter(int index, Object value) {
        parameters.put(index, value);

        return this;
    }
}
