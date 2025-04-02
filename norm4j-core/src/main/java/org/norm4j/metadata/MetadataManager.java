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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.norm4j.Array;
import org.norm4j.Column;
import org.norm4j.Enumerated;
import org.norm4j.FieldGetter;
import org.norm4j.Join;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Id;
import org.norm4j.IdClass;
import org.norm4j.SequenceGenerator;
import org.norm4j.Table;
import org.norm4j.TableGenerator;
import org.norm4j.Temporal;
import org.norm4j.dialects.SQLDialect;

public class MetadataManager
{
    private final Map<Class<?>, TableMetadata> metadataMap;
    private SQLDialect dialect;

    public MetadataManager()
    {
        metadataMap = new HashMap<>();
    }

    public SQLDialect getDialect()
    {
        return dialect;
    }

    public SQLDialect getDialect(Connection connection)
    {
        if (dialect == null)
        {
            dialect = SQLDialect.detectDialect(connection);
        }

        return dialect;
    }

    public void registerTable(Class<?> tableClass)
    {
        Table tableAnnotation;
        TableMetadata table;
        String tableName;
        String schema;
        Class<?> idClass;
        Join[] joins;

        if (!tableClass.isAnnotationPresent(Table.class))
        {
            throw new IllegalArgumentException("Class " 
                    + tableClass.getName()
                    + " is not annotated with @Table");
        }

        tableAnnotation = tableClass.getAnnotation(Table.class);

        if (tableAnnotation.name().isEmpty())
        {
            tableName = tableClass.getSimpleName();
        }
        else
        {
            tableName = tableAnnotation.name();
        }

        schema = tableAnnotation.schema();

        if (tableClass.isAnnotationPresent(IdClass.class))
        {
            idClass = tableClass.getAnnotation(IdClass.class).value();
        }
        else
        {
            idClass = null;
        }

        joins = tableClass.getAnnotationsByType(Join.class);

        for (Join join : joins)
        {
            if (join.columns().length == 0 ||
                    join.reference().columns().length == 0)
            {
                throw new IllegalArgumentException("Missing column in join.");
            }

            if (join.columns().length != join.reference().columns().length)
            {
                throw new IllegalArgumentException("Different number of columns in join.");
            }
        }

        table = new TableMetadata(
                tableClass,
                tableName,
                schema,
                idClass,
                joins);

        for (Field field : tableClass.getDeclaredFields())
        {
            Map<Class<?>, Object> annotations;

            annotations = new HashMap<>();

            if (field.isAnnotationPresent(Column.class))
            {
                Column column;

                column = field.getAnnotation(Column.class);

                annotations.put(Column.class, column);
            }

            if (field.isAnnotationPresent(Array.class))
            {
                annotations.put(Array.class, field.getAnnotation(Array.class));
            }

            if (field.isAnnotationPresent(Id.class))
            {
                annotations.put(Id.class, field.getAnnotation(Id.class));
            }

            if (field.isAnnotationPresent(GeneratedValue.class))
            {
                annotations.put(GeneratedValue.class, field.getAnnotation(GeneratedValue.class));
            }

            if (field.isAnnotationPresent(SequenceGenerator.class))
            {
                annotations.put(SequenceGenerator.class, field.getAnnotation(SequenceGenerator.class));
            }

            if (field.isAnnotationPresent(TableGenerator.class))
            {
                annotations.put(TableGenerator.class, field.getAnnotation(TableGenerator.class));
            }

            if (field.isAnnotationPresent(Temporal.class))
            {
                annotations.put(Temporal.class, field.getAnnotation(Temporal.class));
            }

            if (field.isAnnotationPresent(Array.class))
            {
                annotations.put(Array.class, field.getAnnotation(Array.class));
            }

            if (field.isAnnotationPresent(Enumerated.class))
            {
                annotations.put(Enumerated.class, field.getAnnotation(Enumerated.class));
            }

            table.getColumns().add(new ColumnMetadata(table, annotations, field));
        }

        metadataMap.put(tableClass, table);
    }

    public TableMetadata getMetadata(Class<?> tableClass)
    {
        return metadataMap.get(tableClass);
    }

    public ColumnMetadata getMetadata(Class<?> tableClass, String columnName)
    {
        TableMetadata tableMetadata;

        tableMetadata = getMetadata(tableClass);

        if (tableMetadata == null)
        {
            throw new IllegalArgumentException("No metadata found for class " 
                    + tableClass.getName());
        }

        return tableMetadata.getColumns().stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .findFirst().get();
    }

    public List<ColumnMetadata> getMetadata(Class<?> tableClass, String[] columnNames)
    {
        List<ColumnMetadata> columns;

        columns = new ArrayList<>();

        for (String columnName : columnNames)
        {
            columns.add(getMetadata(tableClass, columnName));
        }

        return columns;
    }

    public <T, R> ColumnMetadata getMetadata(FieldGetter<T, R> fieldGetter)
    {
        FieldGetterMetadata fieldGetterMetadata;
        TableMetadata tableMetadata;

        fieldGetterMetadata = extractMetadata(fieldGetter);

        tableMetadata = getMetadata(fieldGetterMetadata.getTableClass());

        if (tableMetadata == null)
        {
            throw new IllegalArgumentException("No metadata found for class " 
                    + fieldGetterMetadata.getTableClass().getName());
        }

        return tableMetadata.getColumns().stream()
                .filter(c -> c.getField().getName().equals(fieldGetterMetadata.getFieldName()))
                .findFirst().get();
    }

    public void createTables(DataSource dataSource)
    {
        try (Connection connection = dataSource.getConnection())
        {
            List<String> existingTableGenerators;
            List<TableMetadata> existingTables;
            SQLDialect dialect;

            dialect = getDialect(connection);

            existingTableGenerators = new ArrayList<>();

            for (Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet())
            {
                TableMetadata tableMetadata;

                tableMetadata = entry.getValue();

                for (ColumnMetadata column : tableMetadata.getColumns())
                {
                    if (column.getAnnotations().containsKey(GeneratedValue.class))
                    {
                        GeneratedValue generatedValue;

                        generatedValue = (GeneratedValue)column.getAnnotations().get(GeneratedValue.class);

                        if (generatedValue.strategy() == GenerationType.TABLE)
                        {
                            String generatorTableName;
                            TableIdGenerator idGenerator;

                            idGenerator = new TableIdGenerator((TableGenerator)column
                                    .getAnnotations().get(TableGenerator.class));

                            generatorTableName = dialect.getTableName(idGenerator.getSchema(), 
                                    idGenerator.getTable());

                            if (!existingTableGenerators.contains(generatorTableName))
                            {
                                if (!dialect.tableExists(connection, 
                                        idGenerator.getSchema(),
                                        idGenerator.getTable()))
                                {
                                    executeUpdate(connection, 
                                            dialect.createSequenceTable(idGenerator.getSchema(), 
                                                    idGenerator.getTable(), 
                                                    idGenerator.getPkColumnName(), 
                                                    idGenerator.getValueColumnName()));
                                }

                                existingTableGenerators.add(generatorTableName);
                            }
                        }
                    }
                }
            }

            existingTables = new ArrayList<>();

            for (Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet())
            {
                TableMetadata tableMetadata;

                tableMetadata = entry.getValue();

                if (dialect.tableExists(connection, 
                        tableMetadata.getSchema(),
                        tableMetadata.getTableName()))
                {
                    existingTables.add(tableMetadata);
                }
            }

            for (Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet())
            {
                TableMetadata tableMetadata;

                tableMetadata = entry.getValue();

                if (!existingTables.stream()
                        .anyMatch(t -> t.getSchema().equals(tableMetadata.getSchema()) &&
                                t.getTableName().equals(tableMetadata.getTableName())))
                {
                    for (ColumnMetadata column : tableMetadata.getColumns())
                    {
                        if (column.getAnnotations().containsKey(GeneratedValue.class))
                        {
                            GeneratedValue generatedValue;
    
                            generatedValue = (GeneratedValue)column.getAnnotations().get(GeneratedValue.class);
    
                            if (generatedValue.strategy() == GenerationType.SEQUENCE)
                            {
                                String schema = "";
                                String sequenceName = "";
                                int initialValue = 1;

                                if (column.getAnnotations().containsKey(SequenceGenerator.class))
                                {
                                    SequenceGenerator sequenceGenerator;
        
                                    sequenceGenerator = (SequenceGenerator)column.getAnnotations()
                                            .get(SequenceGenerator.class);
        
                                    schema = sequenceGenerator.schema();

                                    sequenceName = sequenceGenerator.sequenceName();

                                    initialValue = sequenceGenerator.initialValue();
                                }

                                if (sequenceName.isEmpty())
                                {
                                    sequenceName = dialect.createSequenceName(tableMetadata, column);
                                }

                                if (!dialect.sequenceExists(connection, 
                                        schema,
                                        sequenceName))
                                {
                                    executeUpdate(connection, 
                                            dialect.createSequence(schema, 
                                                    sequenceName, 
                                                    initialValue));
                                }
                            }
                        }
                    }

                    executeUpdate(connection, dialect.createTable(tableMetadata));
                }
            }

            for (Entry<Class<?>, TableMetadata> entry : metadataMap.entrySet())
            {
                TableMetadata tableMetadata;
                Class<?> tableClass;

                tableClass = entry.getKey();

                tableMetadata = metadataMap.get(tableClass);

                if (!existingTables.stream()
                        .anyMatch(t -> t.getSchema().equals(tableMetadata.getSchema()) &&
                                t.getTableName().equals(tableMetadata.getTableName())))
                {
                    for (Join join : entry.getValue().getJoins())
                    {
                        if (join.referencialIntegrity())
                        {
                            executeUpdate(connection, 
                                    dialect.alterTable(tableMetadata, 
                                            metadataMap.get(join.reference().table()),
                                            join));
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void executeUpdate(Connection connection, String sql)
    {
        try (Statement stmt = connection.createStatement())
        {
            stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private <T, R> FieldGetterMetadata extractMetadata(FieldGetter<T, R> getter)
    {
        Method getImplMethodName;
        Method getImplClass;
        String implClass;
        Object serializedLambda;
        Method writeReplace;
        String className;
        String methodName;

        try
        {
            writeReplace = getter.getClass().getDeclaredMethod("writeReplace");

            writeReplace.setAccessible(true);

            serializedLambda = writeReplace.invoke(getter);

            getImplClass = serializedLambda.getClass()
                    .getDeclaredMethod("getImplClass");

            getImplMethodName = serializedLambda.getClass()
                    .getDeclaredMethod("getImplMethodName");

            implClass = (String) getImplClass.invoke(serializedLambda);

            className = implClass.replace('/', '.');

            methodName = (String) getImplMethodName.invoke(serializedLambda);

            if (methodName.startsWith("get") && methodName.length() > 3)
            {
                return new FieldGetterMetadata(Class.forName(className), 
                        Character.toLowerCase(methodName.charAt(3))
                                + methodName.substring(4));
            }
            else if (methodName.startsWith("is") && methodName.length() > 2)
            {
                return new FieldGetterMetadata(Class.forName(className), 
                        Character.toLowerCase(methodName.charAt(2))
                                + methodName.substring(3));
            }

            return new FieldGetterMetadata(Class.forName(className), methodName);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private class FieldGetterMetadata
    {
        private final Class<?> tableClass;
        private final String fieldName;
    
        public FieldGetterMetadata(Class<?> tableClass, String fieldName)
        {
            this.tableClass = tableClass;
    
            this.fieldName = fieldName;
        }
    
        public Class<?> getTableClass()
        {
            return tableClass;
        }
    
        public String getFieldName()
        {
            return fieldName;
        }
    }
}
