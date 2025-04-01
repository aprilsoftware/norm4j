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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.norm4j.EnumType;
import org.norm4j.Enumerated;
import org.norm4j.Join;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.SequenceGenerator;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public abstract class GenericDialect implements SQLDialect
{
    public GenericDialect()
    {
    }

    public boolean isArraySupported()
    {
        return false;
    }

    public boolean isSequenceSupported()
    {
        return true;
    }

    public boolean isGeneratedKeysForSequenceSupported()
    {
        return true;
    }

    public String getTableName(String schema, String tableName)
    {
        if (schema == null ||
            schema.isEmpty())
        {
            return tableName;
        }
        else
        {
            return schema
                    + "."
                    + tableName;
        }
    }

    public String getSequenceName(TableMetadata table, ColumnMetadata column)
    {
        SequenceGenerator sequenceGenerator;
        String sequenceName;

        sequenceGenerator = (SequenceGenerator)column.getAnnotations()
                .get(SequenceGenerator.class);

        if (sequenceGenerator == null || sequenceGenerator.sequenceName().isEmpty())
        {
            sequenceName = createSequenceName(table, column);
        }
        else
        {
            sequenceName = sequenceGenerator.sequenceName();
        }

        if (sequenceGenerator != null &&
            !sequenceGenerator.schema().isEmpty())
        {
            return sequenceGenerator.schema()
                    + "."
                    + sequenceName;
        }
        else
        {
            return sequenceName;
        }
    }

    public String createSequenceName(TableMetadata table, 
            ColumnMetadata column)
    {
        return table.getTableName()
                + "_"
                + column.getColumnName()
                + "_seq";
    }

    public String getForeignKeyName(TableMetadata table, TableMetadata referenceTable, Join foreignKey)
    {
        if (foreignKey.name().isEmpty())
        {
            return table.getTableName()
                    + "_"
                    + referenceTable.getTableName();
        }
        else
        {
            return foreignKey.name();
        }
    }

    public String alterTable(TableMetadata table, 
            TableMetadata referenceTable,
            Join foreignKey)
    {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("ALTER TABLE ");
        ddl.append(getTableName(table));
        ddl.append(" ADD CONSTRAINT ");
        ddl.append(getForeignKeyName(table, 
                referenceTable, 
                foreignKey));
        ddl.append(" FOREIGN KEY (");

        for (int i = 0; i < foreignKey.columns().length; i++)
        {
            if (i > 0)
            {
                ddl.append(", ");
            }

            ddl.append(foreignKey.columns()[i]);
        }

        ddl.append(") REFERENCES ");
        ddl.append(getTableName(referenceTable.getSchema(), 
                referenceTable.getTableName()));
        ddl.append(" (");

        for (int i = 0; i < foreignKey.reference().columns().length; i++)
        {
            String columnName;

            columnName = foreignKey.reference().columns()[i];

            if (i > 0)
            {
                ddl.append(", ");
            }

            ddl.append(columnName);
        }

        ddl.append(")");

        return ddl.toString();
    }

    public PreparedStatement createPersistStatement(Connection connection, 
            TableMetadata table)
    {
        StringBuilder sql;
        StringBuilder values;
        int index;

        sql = new StringBuilder();

        sql.append("INSERT INTO ");
        sql.append(getTableName(table));
        sql.append(" (");

        values = new StringBuilder();

        index = 1;

        for (ColumnMetadata column : table.getColumns())
        {
            GeneratedValue generatedValue;

            generatedValue = (GeneratedValue)column.getAnnotations()
                    .get(GeneratedValue.class);

            if (generatedValue == null ||
                    (generatedValue != null &&
                    generatedValue.strategy() != GenerationType.AUTO &&
                    generatedValue.strategy() != GenerationType.IDENTITY &&
                    generatedValue.strategy() != GenerationType.SEQUENCE))
            {
                if (index > 1)
                {
                    sql.append(", ");
    
                    values.append(", ");
                }
    
                sql.append(column.getColumnName());
    
                values.append("?");
    
                index++;
            }
        }

        sql.append(") VALUES (");
        sql.append(values);
        sql.append(")");

        try
        {
            return connection.prepareStatement(sql.toString(), 
                    Statement.RETURN_GENERATED_KEYS);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object fromSqlValue(ColumnMetadata column, Object value)
    {
        if (value != null)
        {
            if (column.getField().getType().isEnum())
            {
                Enumerated  enumerated;
    
                enumerated = (Enumerated)column.getAnnotations()
                        .get(Enumerated.class);
    
                if (enumerated == null || enumerated.value() == EnumType.ORDINAL)
                {
                    value = column.getField().getType().getEnumConstants()[(int)value];
                }
                else
                {
                    value = Enum.valueOf(column.getField().getType()
                            .asSubclass(Enum.class), (String)value);
                }
            }
            else if (column.getField().getType() == UUID.class
                    && value instanceof String)
            {
                value = UUID.fromString((String)value);
            }
            if (column.getField().getType() == java.sql.Date.class &&
                    value instanceof java.sql.Time)
            {
                value = new java.sql.Date(((java.sql.Time)value).getTime());
            }
            else if (column.getField().getType() == java.sql.Date.class &&
                    value instanceof java.sql.Timestamp)
            {
                value = new java.sql.Date(((java.sql.Timestamp)value).getTime());
            }
        }

        return value;
    }

    public Object toSqlValue(ColumnMetadata column, Object value)
    {
        if (value != null)
        {
            if (column.getField().getType().isEnum())
            {
                Enumerated  enumerated;

                enumerated = (Enumerated)column.getAnnotations()
                        .get(Enumerated.class);

                if (enumerated == null || enumerated.value() == EnumType.ORDINAL)
                {
                    value = ((Enum<?>)value).ordinal();
                }
                else
                {
                    value = ((Enum<?>)value).name();
                }
            }
        }

        return value;
    }
}
