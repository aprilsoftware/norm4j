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
package org.norm4j.dialects;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.norm4j.Array;
import org.norm4j.ArrayType;
import org.norm4j.Column;
import org.norm4j.EnumType;
import org.norm4j.Enumerated;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Temporal;
import org.norm4j.TemporalType;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;

public class PostgreSQLDialect extends GenericDialect
{
    public PostgreSQLDialect()
    {
    }

    public boolean isDialect(String productName)
    {
        return productName.toLowerCase()
                .contains("postgresql");
    }

    public boolean isArraySupported()
    {
        return true;
    }

    public String createSequence(String schema, 
            String sequenceName, 
            int initialValue)
    {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("CREATE SEQUENCE ");
        if (!schema.isEmpty())
        {
            ddl.append(schema);
            ddl.append(".");
        }
        ddl.append(sequenceName);
        ddl.append(" START ");
        ddl.append(initialValue);
        ddl.append(";");

        return ddl.toString();
    }

    public String createTable(TableMetadata table)
    {
        List<ColumnMetadata> primaryKeys;
        List<ColumnMetadata> columns;
        StringBuilder ddl;

        columns = table.getColumns();

        ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(table));
        ddl.append(" (");

        primaryKeys = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++)
        {
            GeneratedValue generatedValueAnnotation;
            ColumnMetadata column;
            Column columnAnnotation;

            column = columns.get(i);

            columnAnnotation = (Column)column.getAnnotations().get(Column.class);

            generatedValueAnnotation = (GeneratedValue)column.getAnnotations()
                    .get(GeneratedValue.class);

            if (i > 0)
            {
                ddl.append(", ");
            }

            ddl.append(column.getColumnName());
            ddl.append(" ");

            if (columnAnnotation == null ||
                columnAnnotation.columnDefinition().isEmpty())
            {
                ddl.append(getSqlType(column));
            }
            else
            {
                ddl.append(columnAnnotation.columnDefinition());
            }

            if (generatedValueAnnotation == null)
            {
                if (column.getField().getType().isPrimitive() ||
                        (columnAnnotation != null && !columnAnnotation.nullable()) ||
                        column.isPrimaryKey())
                {
                    ddl.append(" NOT NULL");
                }
            }
            else
            {
                if (generatedValueAnnotation.strategy() == GenerationType.AUTO ||
                        generatedValueAnnotation.strategy() == GenerationType.IDENTITY)
                {
                    ddl.append(" GENERATED ALWAYS AS IDENTITY");
                }
                else if (generatedValueAnnotation.strategy() == GenerationType.SEQUENCE)
                {
                    ddl.append(" NOT NULL DEFAULT nextval('");
                    ddl.append(getSequenceName(table, column));
                    ddl.append("')");
                }
            }

            if (column.isPrimaryKey())
            {
                primaryKeys.add(column);
            }
        }

        if (!primaryKeys.isEmpty())
        {
            ddl.append(", CONSTRAINT ");
            ddl.append(table.getTableName());
            ddl.append("_pkey PRIMARY KEY (");

            for (int i = 0; i < primaryKeys.size(); i++)
            {
                if (i > 0)
                {
                    ddl.append(", ");
                }

                ddl.append(primaryKeys.get(i).getColumnName());
            }

            ddl.append(")");
        }

        ddl.append(");");

        return ddl.toString();
    }

    public String createSequenceTable(String schema, 
            String tableName, 
            String pkColumnName, 
            String valueColumnName)
    {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(schema, tableName));
        ddl.append(" (");
        ddl.append(pkColumnName);
        ddl.append(" character varying(255) NOT NULL, ");
        ddl.append(valueColumnName);
        ddl.append(" bigint NOT NULL, CONSTRAINT ");
        ddl.append(tableName);
        ddl.append("_pkey PRIMARY KEY (");
        ddl.append(pkColumnName);
        ddl.append("));");

        return ddl.toString();
    }

    public boolean sequenceExists(Connection connection, 
            String schema, 
            String sequenceName)
    {
        return exists(connection, schema, sequenceName, "SEQUENCE");
    }

    public boolean tableExists(Connection connection, 
            String schema, 
            String tableName)
    {
        return exists(connection, schema, tableName, "TABLE");
    }

    private boolean exists(Connection connection, 
            String schema, 
            String objectName, 
            String objectType)
    {
        DatabaseMetaData dbMetaData;

        if (schema != null && schema.isEmpty())
        {
            schema = null;
        }

        try
        {
            dbMetaData = connection.getMetaData();

            try (ResultSet rs = dbMetaData.getTables(null,
                    schema, 
                    objectName, 
                    new String[] {objectType}))
            {
                return rs.next();
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object toSqlValue(ColumnMetadata column, Object value)
    {
        if (value instanceof java.util.Date)
        {
            Temporal temporal;

            temporal = (Temporal)column.getAnnotations()
                    .get(Temporal.class);

            if (temporal == null || temporal.value() == TemporalType.DATE)
            {
                value = new java.sql.Date(((java.util.Date)value).getTime());
            }
            else if (temporal.value() == TemporalType.TIME)
            {
                value = new java.sql.Time(((java.util.Date)value).getTime());
            }
            else
            {
                value = new java.sql.Timestamp(((java.util.Date)value).getTime());
            }
        }
        else if (value instanceof java.sql.Date)
        {
            Temporal temporal;

            temporal = (Temporal)column.getAnnotations()
                    .get(Temporal.class);

            if (temporal != null)
            {
                if (temporal.value() == TemporalType.TIME)
                {
                    value = new java.sql.Time(((java.sql.Date)value).getTime());
                }
                else
                {
                    value = new java.sql.Timestamp(((java.sql.Date)value).getTime());
                }
            }
        }
        else
        {
            value = super.toSqlValue(column, value);
        }

        return value;
    }

    public Object fromSqlValue(ColumnMetadata column, Object value)
    {
        if (value != null &&
            column.getField().getType().isArray())
        {
            if (value instanceof PGobject)
            {
                value = parseVector(((PGobject)value).getValue());
            }
            else
            {
                try
                {
                    value = ((PgArray)value).getArray();
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }

                if (column.getField().getType().getComponentType() == boolean.class)
                {
                    boolean[] primitiveArray;
                    Boolean[] array;

                    array = (Boolean[])value;

                    primitiveArray = new boolean[array.length];

                    for (int i = 0; i < array.length; i++)
                    {
                        primitiveArray[i] = array[i];
                    }

                    value = primitiveArray;
                }
                else if (column.getField().getType().getComponentType() == int.class)
                {
                    int[] primitiveArray;
                    Integer[] array;

                    array = (Integer[])value;

                    primitiveArray = new int[array.length];

                    for (int i = 0; i < array.length; i++)
                    {
                        primitiveArray[i] = array[i];
                    }

                    value = primitiveArray;
                }
                else if (column.getField().getType().getComponentType() == long.class)
                {
                    long[] primitiveArray;
                    Long[] array;

                    array = (Long[])value;

                    primitiveArray = new long[array.length];

                    for (int i = 0; i < array.length; i++)
                    {
                        primitiveArray[i] = array[i];
                    }

                    value = primitiveArray;
                }
                else if (column.getField().getType().getComponentType() == float.class)
                {
                    float[] primitiveArray;
                    Float[] array;

                    array = (Float[])value;

                    primitiveArray = new float[array.length];

                    for (int i = 0; i < array.length; i++)
                    {
                        primitiveArray[i] = array[i];
                    }

                    value = primitiveArray;
                }
                else if (column.getField().getType().getComponentType() == double.class)
                {
                    double[] primitiveArray;
                    Double[] array;

                    array = (Double[])value;

                    primitiveArray = new double[array.length];

                    for (int i = 0; i < array.length; i++)
                    {
                        primitiveArray[i] = array[i];
                    }

                    value = primitiveArray;
                }
            }
        }
        else
        {
            value = super.fromSqlValue(column, value);
        }

        return value;
    }

    public String limitSelect(int offset, int limit)
    {
        return "LIMIT "
            + limit
            + " OFFSET "
            + offset;
    }

    private float[] parseVector(String value)
    {
        String[] elements;
        float[] values;

        elements = value.substring(1, value.length() - 1).split(",");

        values = new float[elements.length];

        for (int i = 0; i < elements.length; i++)
        {
            values[i] = Float.parseFloat(elements[i].trim());
        }

        return values;
    }

    private String getSqlType(ColumnMetadata column)
    {
        Column columnAnnotation;
        Class<?> fieldType;
        
        fieldType = column.getField().getType();

        columnAnnotation = (Column)column.getAnnotations().get(Column.class);

        if (fieldType == java.util.Date.class || 
                fieldType == java.sql.Date.class)
        {
            Temporal temporalAnnotation;

            temporalAnnotation = (Temporal)column.getAnnotations().get(Temporal.class);

            if (temporalAnnotation == null ||
                    temporalAnnotation.value() == TemporalType.DATE)
            {
                return "date";
            }
            else if (temporalAnnotation.value() == TemporalType.TIME)
            {
                return "time";
            }
            else
            {
                return "timestamp";
            }
        }
        else if (column.getField().getType().isArray())
        {
            Array arraryAnnotation;

            arraryAnnotation = (Array)column.getAnnotations().get(Array.class);

            if (arraryAnnotation == null ||
                arraryAnnotation.type() == ArrayType.Array)
            {
                if (columnAnnotation == null)
                {
                    return getSqlType(column.getField()
                            .getType().getComponentType(), 0)
                            + "[]";
                }
                else
                {
                    return getSqlType(column.getField()
                            .getType().getComponentType(),
                            columnAnnotation.length())
                            + "[]";
                }
            }
            else if (arraryAnnotation.type() == ArrayType.Vector)
            {
                return "vector(" + arraryAnnotation.length() + ")";
            }
            else
            {
                throw new RuntimeException("Unsupported array type.");
            }
        }
        else if (column.getField().getType().isEnum())
        {
            Enumerated enumeratedAnnotation;

            enumeratedAnnotation = (Enumerated)column.getAnnotations()
                    .get(Enumerated.class);

            if (enumeratedAnnotation == null)
            {
                return "int";
            }
            else
            {
                if (enumeratedAnnotation.value() == EnumType.ORDINAL)
                {
                    return "int";
                }
                else if (enumeratedAnnotation.value() == EnumType.STRING)
                {
                    return "character varying(255)";
                }
                else
                {
                    throw new RuntimeException("Unsupported enum type.");
                }
            }
        }
        else
        {
            if (columnAnnotation == null)
            {
                return getSqlType(fieldType, 255);
            }
            else
            {
                return getSqlType(fieldType, columnAnnotation.length());
            }
        }
    }

    private String getSqlType(Class<?> fieldType, int length)
    {
        if (fieldType == String.class)
        {
            return "character varying(" + length + ")";
        }
        else if (fieldType == boolean.class || fieldType == Boolean.class)
        {
            return "boolean";
        }
        else if (fieldType == int.class || fieldType == Integer.class)
        {
            return "int";
        }
        else if (fieldType == long.class || fieldType == Long.class)
        {
            return "bigint";
        }
        else if (fieldType == float.class || fieldType == Float.class)
        {
            return "real";
        }
        else if (fieldType == double.class || fieldType == Double.class)
        {
            return "double precision";
        }
        else if (fieldType == BigDecimal.class)
        {
            return "numeric";
        }
        else if (fieldType == java.util.Date.class || 
                fieldType == java.sql.Date.class)
        {
            return "date";
        }
        else if (fieldType == java.sql.Time.class)
        {
            return "time";
        }
        else if (fieldType == java.sql.Timestamp.class)
        {
            return "timestamp";
        }
        else if (fieldType == UUID.class)
        {
            return "uuid";
        }

        throw new RuntimeException("Unsupported SQL type.");
    }
}
