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

import org.norm4j.*;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.helpers.TableCreationHelper;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.norm4j.metadata.helpers.TableCreationHelper.validateJoins;

public class MetadataManager {
    private final Map<Class<?>, TableMetadata> metadataMap;
    private SQLDialect dialect;
    private final Map<FieldGetter<?, ?>, FieldGetterMetadata> getterCache = new ConcurrentHashMap<>();
    public static final List<Class<? extends Annotation>> annotationTypes = List.of(
            Column.class,
            Array.class,
            Id.class,
            GeneratedValue.class,
            SequenceGenerator.class,
            TableGenerator.class,
            Temporal.class,
            Enumerated.class);

    public MetadataManager() {
        metadataMap = new HashMap<>();
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public SQLDialect getDialect(Connection connection) {
        if (dialect == null) {
            dialect = SQLDialect.detectDialect(connection);
        }

        return dialect;
    }

    public void registerPackage(String packageName) {
        for (Class c : getPackageClasses(packageName)) {
            if (c.isAnnotationPresent(Table.class)) {
                registerTable(c);
            }
        }
    }

    private List<Class> getPackageClasses(String packageName) {
        InputStream inputStream;
        BufferedReader reader;

        inputStream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));

        reader = new BufferedReader(new InputStreamReader(inputStream));

        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toList());
    }

    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerTable(Class<?> tableClass) {
        if (!tableClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class " + tableClass.getName()
                    + " is not annotated with @Table");
        }

        Table tableAnnotation = tableClass.getAnnotation(Table.class);
        String tableName = tableAnnotation.name().isEmpty()
                ? tableClass.getSimpleName().toLowerCase()
                : tableAnnotation.name();
        String schema = tableAnnotation.schema();

        Class<?> idClass = tableClass.isAnnotationPresent(IdClass.class)
                ? tableClass.getAnnotation(IdClass.class).value()
                : null;

        Join[] joins = tableClass.getAnnotationsByType(Join.class);
        validateJoins(joins);

        TableMetadata tableMetadata = new TableMetadata(
                tableClass,
                tableName,
                schema,
                idClass,
                joins);

        for (Field field : tableClass.getDeclaredFields()) {
            Map<Class<?>, Object> annotations = new HashMap<>();

            for (Class<? extends Annotation> annotationType : annotationTypes) {
                Annotation annotation = field.getAnnotation(annotationType);
                if (annotation != null) {
                    annotations.put(annotationType, annotation);
                }
            }

            tableMetadata.getColumns().add(new ColumnMetadata(tableMetadata, annotations, field));
        }

        metadataMap.put(tableClass, tableMetadata);
    }

    public TableMetadata getMetadata(Class<?> tableClass) {
        return metadataMap.get(tableClass);
    }

    public ColumnMetadata getMetadata(Class<?> tableClass, String columnName) {
        TableMetadata tableMetadata;

        tableMetadata = getMetadata(tableClass);

        if (tableMetadata == null) {
            throw new IllegalArgumentException("No metadata found for class "
                    + tableClass.getName());
        }

        return tableMetadata.getColumns().stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Column not found: " + columnName));
    }

    public List<ColumnMetadata> getMetadata(Class<?> tableClass, String[] columnNames) {
        return Arrays.stream(columnNames)
                .map(name -> getMetadata(tableClass, name))
                .collect(Collectors.toList());
    }

    public <T, R> ColumnMetadata getMetadata(FieldGetter<T, R> fieldGetter) {
        FieldGetterMetadata fieldGetterMetadata;
        TableMetadata tableMetadata;

        fieldGetterMetadata = getterCache.computeIfAbsent(fieldGetter, this::extractMetadata);

        tableMetadata = getMetadata(fieldGetterMetadata.getTableClass());

        if (tableMetadata == null) {
            throw new IllegalArgumentException("No metadata found for class "
                    + fieldGetterMetadata.getTableClass().getName());
        }

        return tableMetadata.getColumns().stream()
                .filter(c -> c.getField().getName().equals(fieldGetterMetadata.getFieldName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Field not found: "
                        + fieldGetterMetadata.getFieldName()));
    }

    public <T, R> boolean compareColumns(TableMetadata table,
            Join join,
            FieldGetter<T, R>... fieldGetters) {
        for (FieldGetter<T, R> fieldGetter : fieldGetters) {
            ColumnMetadata column;

            column = getMetadata(fieldGetter);

            if (table.getTableName().equals(column.getTable().getTableName())) {
                if (!Arrays.asList(join.columns()).contains(column.getColumnName())) {
                    return false;
                }
            } else {
                TableMetadata referenceTableMetadata;

                referenceTableMetadata = getMetadata(join.reference().table());

                if (referenceTableMetadata == null) {
                    throw new IllegalArgumentException("No metadata found for class "
                            + join.reference().table().getName());
                }

                if (referenceTableMetadata.getTableName().equals(column.getTable().getTableName())) {
                    if (!Arrays.asList(join.reference().columns()).contains(column.getColumnName())) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public void createTables(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            SQLDialect dialect = getDialect(connection);
            TableCreationHelper helper = new TableCreationHelper(metadataMap, this::executeUpdate);

            // Create sequence tables for table generators
            helper.createSequenceTables(connection, dialect);

            // Identify existing tables
            List<TableMetadata> existingTables = helper.getExistingTables(connection, dialect);

            // Create tables and sequences
            helper.createTablesAndSequences(connection, dialect, existingTables);

            // Add foreign key constraints
            helper.addForeignKeyConstraints(connection, dialect, existingTables);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeUpdate(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    private <T, R> FieldGetterMetadata extractMetadata(FieldGetter<T, R> getter) {
        return FieldGetterMetadata.extractMetadata(getter);
    }
}
