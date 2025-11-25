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

import org.norm4j.Array;
import org.norm4j.Column;
import org.norm4j.Enumerated;
import org.norm4j.FieldGetter;
import org.norm4j.GeneratedValue;
import org.norm4j.Id;
import org.norm4j.IdClass;
import org.norm4j.Join;
import org.norm4j.SequenceGenerator;
import org.norm4j.Table;
import org.norm4j.TableGenerator;
import org.norm4j.Temporal;
import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.helpers.DdlHelper;
import org.norm4j.metadata.helpers.TableCreationHelper;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
        metadataMap = new LinkedHashMap<>();
    }

    public MetadataManager(SQLDialect dialect) {
        this();

        this.dialect = dialect;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public SQLDialect initDialect(Connection connection) {
        if (dialect == null) {
            dialect = SQLDialect.detectDialect(connection);
        }

        return dialect;
    }

    public void registerPackage(String packageName) {
        for (Class<?> c : getPackageClasses(packageName)) {
            if (c.isAnnotationPresent(Table.class)) {
                registerTable(c);
            }
        }
    }

    private List<Class<?>> getPackageClasses(String packageName) {
        InputStream inputStream;
        BufferedReader reader;

        inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));

        reader = new BufferedReader(new InputStreamReader(inputStream));

        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toList());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerPackageFromClassPath(String packageName) {
        registerPackageFromClassPath(packageName, Thread.currentThread().getContextClassLoader());
    }

    public void registerPackageFromClassPath(String packageName, ClassLoader classLoader) {
        for (Class<?> c : getPackageClassesFromClassPath(packageName, classLoader)) {
            if (c.isAnnotationPresent(Table.class)) {
                registerTable(c);
            }
        }
    }

    private List<Class<?>> getPackageClassesFromClassPath(String packageName, ClassLoader cl) {
        String path = packageName.replace('.', '/');
        List<Class<?>> classes = new ArrayList<>();

        try {
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    File dir = new File(url.toURI());
                    if (dir.isDirectory()) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                String name = f.getName();
                                if (name.endsWith(".class") && !name.contains("$")) {
                                    String simpleName = name.substring(0, name.length() - 6);
                                    String fqcn = packageName + "." + simpleName;
                                    classes.add(Class.forName(fqcn, false, cl));
                                }
                            }
                        }
                    }
                } else if ("jar".equals(protocol)) {
                    String urlPath = url.getPath();
                    int bang = urlPath.indexOf('!');
                    if (bang != -1) {
                        String jarPath = urlPath.substring("file:".length(), bang);
                        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                            String prefix = path + "/";
                            Enumeration<JarEntry> entries = jar.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith(prefix)
                                        && name.endsWith(".class")
                                        && !name.contains("$")
                                        && name.indexOf('/', prefix.length()) == -1) {

                                    String fqcn = name.substring(0, name.length() - 6)
                                            .replace('/', '.');
                                    classes.add(Class.forName(fqcn, false, cl));
                                }
                            }
                        }
                    }
                }
            }

            return classes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package " + packageName, e);
        }
    }

    public void registerTable(Class<?> tableClass) {
        registerTable(tableClass, null, null);
    }

    public void registerTable(Class<?> tableClass, String schema, String tableName) {
        if (!tableClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class " + tableClass.getName()
                    + " is not annotated with @Table");
        }

        Table tableAnnotation = tableClass.getAnnotation(Table.class);

        tableName = (tableName != null && !tableName.isEmpty()) ? tableName
                : tableAnnotation.name().isEmpty()
                        ? tableClass.getSimpleName().toLowerCase()
                        : tableAnnotation.name();

        schema = (schema != null && !schema.isEmpty()) ? schema : tableAnnotation.schema();

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
            Map<Class<?>, Object> annotations = new LinkedHashMap<>();

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

    public List<TableMetadata> getTableMetadata() {
        return new ArrayList<>(metadataMap.values());
    }

    public TableMetadata getTableMetadata(Class<?> tableClass) {
        return metadataMap.get(tableClass);
    }

    public ColumnMetadata getColumnMetadata(Class<?> tableClass, String columnName) {
        TableMetadata tableMetadata;

        tableMetadata = getTableMetadata(tableClass);

        if (tableMetadata == null) {
            throw new IllegalArgumentException("No metadata found for class "
                    + tableClass.getName());
        }

        return tableMetadata.getColumns().stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Column not found: " + columnName));
    }

    public List<ColumnMetadata> getColumnMetadata(Class<?> tableClass, String[] columnNames) {
        return Arrays.stream(columnNames)
                .map(name -> getColumnMetadata(tableClass, name))
                .collect(Collectors.toList());
    }

    public <T, R> ColumnMetadata getColumnMetadata(FieldGetter<T, R> fieldGetter) {
        FieldGetterMetadata fieldGetterMetadata;
        TableMetadata tableMetadata;

        fieldGetterMetadata = getterCache.computeIfAbsent(fieldGetter, this::extractMetadata);

        tableMetadata = getTableMetadata(fieldGetterMetadata.getTableClass());

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

    private <T, R> FieldGetterMetadata extractMetadata(FieldGetter<T, R> getter) {
        return FieldGetterMetadata.extractMetadata(getter);
    }

    public List<SequenceMetadata> getSequenceMetadata(TableMetadata tableMetadata) {
        return new DdlHelper(metadataMap).getSequences(tableMetadata, dialect);
    }

    public List<ForeignKeyMetadata> getForeignKeyMetadata(TableMetadata tableMetadata) {
        return new DdlHelper(metadataMap).getForeignKeys(tableMetadata, dialect);
    }

    @SafeVarargs
    public final <T, R> boolean compareColumns(TableMetadata table,
            Join join,
            FieldGetter<T, R>... fieldGetters) {
        for (FieldGetter<T, R> fieldGetter : fieldGetters) {
            ColumnMetadata column;

            column = getColumnMetadata(fieldGetter);

            if (table.getTableName().equals(column.getTable().getTableName())) {
                if (!Arrays.asList(join.columns()).contains(column.getColumnName())) {
                    return false;
                }
            } else {
                TableMetadata referenceTableMetadata;

                referenceTableMetadata = getTableMetadata(join.reference().table());

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

    public void createDdlAsResource(Path resourcePath) {
        DdlHelper helper = new DdlHelper(metadataMap);
        List<String> statements = new ArrayList<>();
        List<String> ddl = new ArrayList<>();

        for (String sql : helper.createSequenceTables(dialect)) {
            ddl.add(sql);
        }

        ddl.sort(String::compareTo);

        statements.addAll(ddl);

        ddl.clear();

        for (TableMetadata tableMetadata : metadataMap.values()) {
            List<String> sequenceDdl = new ArrayList<>();

            for (String sql : helper.createSequences(tableMetadata, dialect)) {
                sequenceDdl.add(sql);
            }

            sequenceDdl.sort(String::compareTo);

            statements.addAll(sequenceDdl);

            ddl.add(dialect.createTable(tableMetadata));
        }

        ddl.sort(String::compareTo);

        statements.addAll(ddl);

        ddl.clear();

        for (String sql : helper.createForeignKeys(dialect)) {
            ddl.add(sql);
        }

        ddl.sort(String::compareTo);

        statements.addAll(ddl);

        ddl.clear();

        try (BufferedWriter writer = Files.newBufferedWriter(resourcePath, StandardCharsets.UTF_8)) {
            for (String statement : statements) {
                writer.write(statement);

                if (!statement.endsWith(";")) {
                    writer.write(";");
                }

                writer.newLine();
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write DDL resource to: " + resourcePath, e);
        }
    }

    public void createTables(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTables(Connection connection) {
        initDialect(connection);

        TableCreationHelper helper = new TableCreationHelper(metadataMap, this::executeUpdate);

        try {
            helper.createSequenceTables(connection, dialect);

            List<TableMetadata> existingTables = helper.getExistingTables(connection, dialect);

            helper.createTablesAndSequences(connection, dialect, existingTables);

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

    private void validateJoins(Join[] joins) {
        for (Join join : joins) {
            if (join.columns().length == 0 || join.reference().columns().length == 0) {
                throw new IllegalArgumentException("Missing column(s) in join.");
            }
            if (join.columns().length != join.reference().columns().length) {
                throw new IllegalArgumentException("Mismatched number of columns in join.");
            }
        }
    }
}
