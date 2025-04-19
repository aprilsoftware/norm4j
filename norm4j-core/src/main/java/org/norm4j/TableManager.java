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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.metadata.TableIdGenerator;
import org.norm4j.metadata.TableMetadata;

public class TableManager
{
    private final MetadataManager metadataManager;
    private final DataSource dataSource;

    public TableManager(DataSource dataSource, MetadataManager metadataManager)
    {
        this.dataSource = dataSource;

        this.metadataManager = metadataManager;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public SQLDialect getDialect()
    {
        if (metadataManager.getDialect() == null)
        {
            try (Connection connection = dataSource.getConnection())
            {
                return metadataManager.getDialect(connection);
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return metadataManager.getDialect();
        }
    }

    public void persist(Object record)
    {
        List<ColumnMetadata> generatedKeyColumns;
        List<ColumnMetadata> outputColumns;
        TableMetadata table;
        Class<?> tableClass;
        int index;

        tableClass = record.getClass();

        table = getTable(tableClass);

        generatedKeyColumns = new ArrayList<>();

        outputColumns = new ArrayList<>();

        index = 1;

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;

            dialect = metadataManager.getDialect(connection);

            try (PreparedStatement ps = dialect.createPersistStatement(connection, table))
            {
                for (ColumnMetadata column : table.getColumns())
                {
                    GeneratedValue generatedValue;

                    generatedValue = (GeneratedValue)column.getAnnotations()
                            .get(GeneratedValue.class);

                    if (generatedValue != null)
                    {
                        if (generatedValue.strategy() == GenerationType.SEQUENCE &&
                                !dialect.isGeneratedKeysForSequenceSupported())
                        {
                            outputColumns.add(column);

                            continue;
                        }
                        else if (generatedValue.strategy() == GenerationType.AUTO || 
                                generatedValue.strategy() == GenerationType.IDENTITY ||
                                (generatedValue.strategy() == GenerationType.SEQUENCE &&
                                        dialect.isGeneratedKeysForSequenceSupported()))
                        {
                            generatedKeyColumns.add(column);

                            continue;
                        }
                        else if (generatedValue.strategy() == GenerationType.TABLE)
                        {
                            TableIdGenerator idGenerator;

                            idGenerator = new TableIdGenerator((TableGenerator)column
                                    .getAnnotations().get(TableGenerator.class));

                            setRecordValue(record, column, idGenerator
                                    .generateId(connection, 
                                            dialect,
                                            table.getTableName()
                                            + "_"
                                            + column.getColumnName()));
                        }
                        else if (generatedValue.strategy() == GenerationType.UUID)
                        {
                            setRecordValue(record, column, UUID.randomUUID());
                        }
                    }

                    setColumnValue(record, column, index, dialect, ps);

                    index++;
                }

                if (outputColumns.isEmpty())
                {
                    ps.executeUpdate();
                }
                else
                {
                    try (ResultSet rs = ps.executeQuery())
                    {
                        if (rs.next())
                        {
                            for (int i = 0; i < outputColumns.size(); i++)
                            {
                                ColumnMetadata outputColumn;

                                outputColumn = outputColumns.get(i);

                                setRecordValue(record,
                                        outputColumn,
                                        rs.getObject(i + 1));
                            }
                        }
                    }
                }

                if (!generatedKeyColumns.isEmpty())
                {
                    try (ResultSet rs = ps.getGeneratedKeys())
                    {
                        if (rs.next())
                        {
                            for (int i = 0; i < generatedKeyColumns.size(); i++)
                            {
                                ColumnMetadata generatedColumn;
                                Object generatedKey;

                                generatedKey = rs.getObject(i + 1);

                                generatedColumn = generatedKeyColumns.get(i);

                                if (generatedKey instanceof Number)
                                {
                                    if (generatedColumn.getField().getType() == short.class ||
                                            generatedColumn.getField().getType() == Short.class)
                                    {
                                        generatedKey = ((Number)generatedKey).shortValue();
                                    }
                                    else if (generatedColumn.getField().getType() == int.class ||
                                            generatedColumn.getField().getType() == Integer.class)
                                    {
                                        generatedKey = ((Number)generatedKey).intValue();
                                    }
                                    else if (generatedColumn.getField().getType() == long.class ||
                                            generatedColumn.getField().getType() == Long.class)
                                    {
                                        generatedKey = ((Number)generatedKey).longValue();
                                    }
                                }

                                setRecordValue(record,
                                        generatedColumn,
                                        generatedKey);
                            }
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

    public <T> T merge(T record)
    {
        List<ColumnMetadata> primaryKeys;
        TableMetadata table;
        Class<?> tableClass;
        int index;

        tableClass = record.getClass();

        table = getTable(tableClass);

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;
            StringBuilder sql;
            
            dialect = metadataManager.getDialect(connection);

            primaryKeys = table.getPrimaryKeys();
    
            if (primaryKeys.isEmpty())
            {
                throw new IllegalStateException("No primary key found for class "
                        + tableClass.getName());
            }
    
            sql = new StringBuilder();
    
            sql.append("UPDATE ");
            sql.append(dialect.getTableName(table));
            sql.append(" SET ");
    
            index = 1;
    
            for (ColumnMetadata column : table.getColumns())
            {
                if (!column.isPrimaryKey())
                {
                    if (index > 1)
                    {
                        sql.append(", ");
                    }
    
                    sql.append(column.getColumnName());
                    sql.append(" = ?");
    
                    index++;
                }
            }
    
            index = 1;
    
            for (ColumnMetadata column : primaryKeys)
            {
                if (index == 1)
                {
                    sql.append(" WHERE ");
                }
                else
                {
                    sql.append(" AND ");
                }
    
                sql.append(column.getColumnName());
                sql.append("=?");
    
                index++;
            }
    
            index = 1;

            try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
            {
                for (ColumnMetadata column : table.getColumns())
                {
                    if (!column.isPrimaryKey())
                    {
                        setColumnValue(record, column, index, dialect, ps);
    
                        index++;
                    }
                }
    
                for (ColumnMetadata column : primaryKeys)
                {
                    setColumnValue(record, column, index, dialect, ps);
    
                    index++;
                }
    
                ps.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return record;
    }

    public void remove(Object record)
    {
        List<ColumnMetadata> primaryKeys;
        TableMetadata table;
        Class<?> tableClass;
        int index;

        tableClass = record.getClass();

        table = getTable(tableClass);

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;
            StringBuilder sql;
            
            dialect = metadataManager.getDialect(connection);

            primaryKeys = table.getPrimaryKeys();

            if (primaryKeys.isEmpty())
            {
                throw new IllegalStateException("No primary key found for class "
                        + tableClass.getName());
            }
    
            sql = new StringBuilder();
    
            sql.append("DELETE FROM ");
            sql.append(dialect.getTableName(table));
    
            index = 1;
    
            for (ColumnMetadata column : primaryKeys)
            {
                if (index == 1)
                {
                    sql.append(" WHERE ");
                }
                else
                {
                    sql.append(" AND ");
                }
    
                sql.append(column.getColumnName());
                sql.append(" = ?");
    
                index++;
            }
    
            index = 1;
            
            try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
            {
                for (ColumnMetadata column : primaryKeys)
                {
                    setColumnValue(record, column, index, dialect, ps);
    
                    index++;
                }
    
                ps.executeUpdate();
            }

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void remove(Class<?> tableClass, Object primaryKey)
    {
        List<ColumnMetadata> primaryKeys;
        TableMetadata table;
        int index;

        table = getTable(tableClass);

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;
            StringBuilder sql;
            
            dialect = metadataManager.getDialect(connection);

            primaryKeys = table.getPrimaryKeys();
    
            if (primaryKeys.isEmpty())
            {
                throw new IllegalStateException("No primary key found for class "
                        + tableClass.getName());
            }
    
            sql = new StringBuilder();
    
            sql.append("DELETE FROM ");
            sql.append(dialect.getTableName(table));
    
            index = 1;
    
            for (ColumnMetadata column : primaryKeys)
            {
                if (index == 1)
                {
                    sql.append(" WHERE ");
                }
                else
                {
                    sql.append(" AND ");
                }
    
                sql.append(column.getColumnName());
                sql.append(" = ?");
    
                index++;
            }

            try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
            {
                setPrimaryKeyValue(tableClass,
                        table,
                        primaryKeys,
                        primaryKey,
                        ps);

                ps.executeUpdate();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T find(Class<T> tableClass, Object primaryKey)
    {
        List<ColumnMetadata> primaryKeys;
        TableMetadata table;
        int index;

        table = getTable(tableClass);

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;
            StringBuilder sql;
            
            dialect = metadataManager.getDialect(connection);

            primaryKeys = table.getPrimaryKeys();

            if (primaryKeys.isEmpty())
            {
                throw new IllegalStateException("No primary key found for class "
                        + tableClass.getName());
            }
    
            sql = new StringBuilder();
    
            sql.append("SELECT ");
    
            index = 1;
    
            for (ColumnMetadata column : table.getColumns())
            {
                if (index > 1)
                {
                    sql.append(", ");
                }
    
                sql.append(column.getColumnName());
    
                index++;
            }
    
            sql.append(" FROM ");
            sql.append(dialect.getTableName(table));
    
            index = 1;
    
            for (ColumnMetadata column : primaryKeys)
            {
                if (index == 1)
                {
                    sql.append(" WHERE ");
                }
                else
                {
                    sql.append(" AND ");
                }
    
                sql.append(column.getColumnName());
                sql.append(" = ?");
    
                index++;
            }

            try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
            {
                List<T> records;

                setPrimaryKeyValue(tableClass, 
                        table, 
                        primaryKeys, 
                        primaryKey, 
                        ps);

                records = listRecords(tableClass, 
                        table,
                        dialect,
                        ps);

                if (records.isEmpty())
                {
                    return null;
                }
                else if (records.size() == 1)
                {
                    return records.get(0);
                }
                else
                {
                    throw new RuntimeException("The query returned more than one record.");
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public final <T, S, R> T joinOne(Object leftRecord, 
            Class<T> rightTableClass, 
            FieldGetter<S, R>... fieldGetters)
    {
        List<T> rightRecords;

        rightRecords = joinMany(leftRecord, 
                rightTableClass, 
                fieldGetters);

        if (rightRecords.isEmpty())
        {
            return null;
        }
        else if (rightRecords.size() == 1)
        {
            return rightRecords.get(0);
        }
        else
        {
            throw new RuntimeException("The query returned more than one record.");
        }
    }

    public <T, R, L> T joinOne(L leftRecord, 
            FieldGetter<L, R> leftFieldGetter,
            Class<T> rightTableClass,
            FieldGetter<T, R> rightFieldGetter)
    {
        return joinOne(leftRecord,
                Arrays.asList(leftFieldGetter),
                rightTableClass,
                Arrays.asList(rightFieldGetter));
    }

    public <T, R, L> T joinOne(L leftRecord, 
            List<FieldGetter<L, R>> leftFieldGetters,
            Class<T> rightTableClass,
            List<FieldGetter<T, R>> rightFieldGetters)
    {
        List<T> rightRecords;

        rightRecords = joinMany(leftRecord, 
                leftFieldGetters, 
                rightTableClass, 
                rightFieldGetters);

        if (rightRecords.isEmpty())
        {
            return null;
        }
        else if (rightRecords.size() == 1)
        {
            return rightRecords.get(0);
        }
        else
        {
            throw new RuntimeException("The query returned more than one record.");
        }
    }

    @SafeVarargs
    public final <T, S, R> List<T> joinMany(Object leftRecord, 
            Class<T> rightTableClass, 
            FieldGetter<S, R>... fieldGetters)
    {
        TableMetadata leftTable;
        Join join;

        leftTable = getTable(leftRecord.getClass());

        join = getJoin(leftTable, 
                rightTableClass, 
                fieldGetters);

        if (join == null)
        {
            TableMetadata rightTable;

            rightTable = getTable(rightTableClass);

            join = getJoin(rightTable, 
                    leftRecord.getClass(), 
                    fieldGetters);
    
            if (join == null)
            {
                throw new IllegalArgumentException("No join found for class " 
                        + leftRecord.getClass().getName());
            }
    
            return join(leftRecord, 
                    metadataManager.getMetadata(leftRecord.getClass(), 
                            join.reference().columns()), 
                    rightTableClass, 
                    metadataManager.getMetadata(rightTableClass, 
                            join.columns()));
        }
        else
        {
            return join(leftRecord, 
                    metadataManager.getMetadata(leftRecord.getClass(), 
                            join.columns()), 
                    rightTableClass, 
                    metadataManager.getMetadata(rightTableClass, 
                            join.reference().columns()));
        }
    }

    private <T, S, R> Join getJoin(TableMetadata leftTable, 
            Class<T> rightTableClass, 
            FieldGetter<S, R>... fieldGetters)
    {
        for (Join join : leftTable.getJoins())
        {
            if (fieldGetters.length > 0)
            {
                if (!getMetadataManager().compareColumns(leftTable, join, fieldGetters))
                {
                    continue;
                }
            }

            if (join.reference().table().equals(rightTableClass))
            {
                return join;
            }
        }

        return null;
    }

    public <T, L, R> List<T> joinMany(L leftRecord, 
            FieldGetter<L, R> leftFieldGetter,
            Class<T> rightTableClass,
            FieldGetter<T, R> rightFieldGetter)
    {
        return joinMany(leftRecord,
                Arrays.asList(leftFieldGetter),
                rightTableClass,
                Arrays.asList(rightFieldGetter));
    }

    public <T, L, R> List<T> joinMany(L leftRecord, 
            List<FieldGetter<L, R>> leftFieldGetters,
            Class<T> rightTableClass,
            List<FieldGetter<T, R>> rightFieldGetters)
    {
        List<ColumnMetadata> leftColumns;
        List<ColumnMetadata> rightColumns;

        leftColumns = new ArrayList<>();

        for (FieldGetter<L, R> fieldGetter : leftFieldGetters)
        {
            leftColumns.add(metadataManager.getMetadata(fieldGetter));
        }

        rightColumns = new ArrayList<>();

        for (FieldGetter<T, R> fieldGetter : rightFieldGetters)
        {
            rightColumns.add(metadataManager.getMetadata(fieldGetter));
        }

        return join(leftRecord, leftColumns, rightTableClass, rightColumns);
    }

    private <T> List<T> join(Object leftRecord, 
            List<ColumnMetadata> leftColumns, 
            Class<T> rightTableClass, 
            List<ColumnMetadata> rightColumns)
    {
        TableMetadata rightTable;
        int index;

        rightTable = getTable(rightTableClass);

        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;
            StringBuilder sql;
            
            dialect = metadataManager.getDialect(connection);


            sql = new StringBuilder();
    
            sql.append("SELECT ");
    
            index = 1;
    
            for (ColumnMetadata column : rightTable.getColumns())
            {
                if (index > 1)
                {
                    sql.append(", ");
                }
    
                sql.append(column.getColumnName());
    
                index++;
            }
    
            sql.append(" FROM ");
            sql.append(dialect.getTableName(rightTable));
    
            index = 1;

            for (ColumnMetadata rightColumn : rightColumns)
            {
                if (index == 1)
                {
                    sql.append(" WHERE ");
                }
                else
                {
                    sql.append(" AND ");
                }

                sql.append(rightColumn.getColumnName());
                sql.append(" = ?");

                index++;
            }

            try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
            {
                index = 1;

                for (ColumnMetadata leftColumn : leftColumns)
                {
                    setColumnValue(leftRecord, leftColumn, index, dialect, ps);

                    index++;
                }

                return listRecords(rightTableClass, 
                        rightTable,
                        dialect,
                        ps);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <R, T> Map<R, List<T>> mapMany(List<R> records, Class<T> rightTableClass)
    {
        final List<ColumnMetadata> primaryKeys;
        Map<R, List<Object>> primaryKeyMap;
        SelectQueryBuilder queryBuilder;
        Class<R> recordClass = null;

        primaryKeys = new ArrayList<>();

        primaryKeyMap = new HashMap<>();

        for (R record : records)
        {
            List<Object> values;

            if (recordClass == null)
            {
                TableMetadata table;

                recordClass = (Class<R>)record.getClass();

                table = getTable(recordClass);

                primaryKeys.addAll(table.getPrimaryKeys());

                if (primaryKeys.isEmpty())
                {
                    throw new IllegalStateException("No primary key found for class "
                            + recordClass.getName());
                }
            }

            values = new ArrayList<>();

            for (ColumnMetadata primaryKey : primaryKeys)
            {
                Field field;

                field = primaryKey.getField();

                field.setAccessible(true);
        
                try
                {
                    values.add(field.get(record));
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }

            primaryKeyMap.put(record, values);
        }

        queryBuilder = createSelectQueryBuilder()
                .select(recordClass)
                .select(rightTableClass)
                .from(recordClass)
                .innerJoin(rightTableClass);

        if (primaryKeys.size() == 1)
        {
            List<Object> values;

            values = new ArrayList<>();

            for (R record : primaryKeyMap.keySet())
            {
                values.add(primaryKeyMap.get(record).get(0));
            }

            queryBuilder.where(primaryKeys.get(0), "in", values);
        }
        else
        {
            // TODO Use tuple if supported, in memory table
            //  or otherwise we can use this technic
            for (R record : primaryKeyMap.keySet())
            {
                queryBuilder.or(q -> 
                {
                    List<Object> values;
                    int index;
    
                    values = primaryKeyMap.get(record);
    
                    index = 0;
    
                    for (ColumnMetadata primaryKey : primaryKeys)
                    {
                        q.and(primaryKey, "=", values.get(index));
    
                        index++;
                    }
                });
            }
        }

        if (recordClass == null)
        {
            return new HashMap<>();
        }
        else
        {
            return queryBuilder.mapResultList(recordClass, rightTableClass);
        }
    }

    private <T> List<T> listRecords(Class<T> tableClass, 
            TableMetadata table,
            SQLDialect dialect,
            PreparedStatement ps)
    {
        try (ResultSet rs = ps.executeQuery())
        {
            List<T> records;

            records = new ArrayList<>();

            while (rs.next())
            {
                T record;

                record = tableClass.getDeclaredConstructor().newInstance();

                for (ColumnMetadata column : table.getColumns())
                {
                    Field field;
                    Object value;

                    field = column.getField();

                    field.setAccessible(true);

                    value = rs.getObject(column.getColumnName());

                    field.set(record, dialect.fromSqlValue(column, value));
                }

                records.add(record);
            }

            return records;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Query createQuery(String query)
    {
        return new Query(this, query);
    }

    public SelectQueryBuilder createSelectQueryBuilder()
    {
        return new SelectQueryBuilder(this);
    }

    public UpdateQueryBuilder createUpdateQueryBuilder()
    {
        return new UpdateQueryBuilder(this);
    }

    public DeleteQueryBuilder createDeleteQueryBuilder()
    {
        return new DeleteQueryBuilder(this);
    }

    private void setRecordValue(Object record, 
            ColumnMetadata column, 
            Object value)
    {
        Field field;
                
        field = column.getField();

        field.setAccessible(true);

        try
        {
            field.set(record, value);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setPrimaryKeyValue(Class<?> tableClass,
            TableMetadata table, 
            List<ColumnMetadata> primaryKeys, 
            Object primaryKey,
            PreparedStatement ps)
    {
        if (table.getIdClass() == null) 
        {
            if (primaryKeys.size() == 1)
            {
                try
                {
                    ps.setObject(1, primaryKey);
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                throw new IllegalStateException("Missing @IdClass "
                        + tableClass.getName());
            }
        }
        else
        {
            Class<?> idClass;
            int index;

            idClass = table.getIdClass();

            if (!idClass.isInstance(primaryKey))
            {
                throw new IllegalArgumentException("Unexpected @IdClass value " 
                        + tableClass.getName());
            }

            index = 1;

            for (ColumnMetadata column : primaryKeys)
            {
                Field field;
                Object value;

                try
                {
                    field = idClass.getDeclaredField(column.getField().getName());

                    field.setAccessible(true);
    
                    value = field.get(primaryKey);
    
                    ps.setObject(index, value);
                }
                catch (SQLException | NoSuchFieldException | IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }

                index++;
            }
        } 
    }

    private void setColumnValue(Object record, 
            ColumnMetadata column, 
            int index, 
            SQLDialect dialect,
            PreparedStatement ps) throws SQLException
    {
        Field field;
        Object value;

        field = column.getField();

        field.setAccessible(true);

        try
        {
            value = field.get(record);

            ps.setObject(index, dialect.toSqlValue(column, value));
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private TableMetadata getTable(Class<?> tableClass)
    {
        TableMetadata table;

        table = metadataManager.getMetadata(tableClass);

        if (table == null)
        {
            throw new IllegalArgumentException("No metadata found for class " 
                    + tableClass.getName());
        }
        else
        {
            return table;
        }
    }
}
