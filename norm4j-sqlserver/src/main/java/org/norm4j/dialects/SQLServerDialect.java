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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.norm4j.Column;
import org.norm4j.EnumType;
import org.norm4j.Enumerated;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.SchemaColumn;
import org.norm4j.schema.SchemaTable;

public class SQLServerDialect extends AbstractDialect {
    public SQLServerDialect() {
    }

    @Override
    public boolean isDialect(String productName) {
        productName = productName.toLowerCase();

        return productName.contains("sql server") ||
                productName.contains("sqlserver");
    }

    @Override
    public boolean isTupleSupported() {
        return false;
    }

    @Override
    public boolean isGeneratedKeysForSequenceSupported() {
        return false;
    }

    @Override
    public String createSequence(String schema,
            String sequenceName,
            int initialValue) {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("CREATE SEQUENCE ");
        if (!schema.isEmpty()) {
            ddl.append(schema);
            ddl.append(".");
        }

        ddl.append(sequenceName);
        ddl.append(" START WITH ");
        ddl.append(initialValue);
        ddl.append(" INCREMENT BY 1;");

        return ddl.toString();
    }

    @Override
    public String createTable(TableMetadata table) {
        List<ColumnMetadata> primaryKeys;
        List<ColumnMetadata> columns;
        StringBuilder ddl;

        columns = table.getColumns();

        ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(table));
        ddl.append(" (");

        primaryKeys = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            GeneratedValue generatedValueAnnotation;
            ColumnMetadata column;
            Column columnAnnotation;

            column = columns.get(i);

            columnAnnotation = (Column) column.getAnnotations().get(Column.class);

            generatedValueAnnotation = (GeneratedValue) column.getAnnotations()
                    .get(GeneratedValue.class);

            if (i > 0) {
                ddl.append(", ");
            }

            ddl.append(column.getColumnName());
            ddl.append(" ");

            if (columnAnnotation == null ||
                    columnAnnotation.columnDefinition().isEmpty()) {
                ddl.append(getSqlType(column));
            } else {
                ddl.append(columnAnnotation.columnDefinition());
            }

            if (generatedValueAnnotation == null) {
                if (column.getField().getType().isPrimitive() ||
                        (columnAnnotation != null && !columnAnnotation.nullable()) ||
                        column.isPrimaryKey()) {
                    ddl.append(" NOT NULL");
                }
            } else {
                if (generatedValueAnnotation.strategy() == GenerationType.AUTO ||
                        generatedValueAnnotation.strategy() == GenerationType.IDENTITY) {
                    ddl.append(" IDENTITY(1,1)");
                } else if (generatedValueAnnotation.strategy() == GenerationType.SEQUENCE) {
                    ddl.append(" DEFAULT NEXT VALUE FOR ");
                    ddl.append(getSequenceName(column));
                }
            }

            if (column.isPrimaryKey()) {
                primaryKeys.add(column);
            }
        }

        if (!primaryKeys.isEmpty()) {
            ddl.append(", PRIMARY KEY (");

            for (int i = 0; i < primaryKeys.size(); i++) {
                if (i > 0) {
                    ddl.append(", ");
                }

                ddl.append(primaryKeys.get(i).getColumnName());
            }

            ddl.append(")");
        }

        ddl.append(");");

        return ddl.toString();
    }

    @Override
    public String createTable(SchemaTable table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String alterTableAddColumn(SchemaTable table, SchemaColumn column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createSequenceTable(String schema,
            String tableName,
            String pkColumnName,
            String valueColumnName) {
        StringBuilder ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(schema, tableName));
        ddl.append(" (");
        ddl.append(pkColumnName);
        ddl.append(" NVARCHAR(255) NOT NULL, ");
        ddl.append(valueColumnName);
        ddl.append(" BIGINT NOT NULL, PRIMARY KEY (");
        ddl.append(pkColumnName);
        ddl.append("));");

        return ddl.toString();
    }

    @Override
    public boolean sequenceExists(Connection connection,
            String schema,
            String sequenceName) {
        String sql;

        sql = "SELECT SCHEMA_NAME(schema_id), name FROM sys.sequences WHERE name = ? AND SCHEMA_NAME(schema_id) = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sequenceName);
            ps.setString(2, schema);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean tableExists(Connection connection,
            String schema,
            String tableName) {
        DatabaseMetaData dbMetaData;

        if (schema == null ||
                (schema != null && schema.isEmpty())) {
            schema = "dbo";
        }

        try {
            dbMetaData = connection.getMetaData();

            try (ResultSet rs = dbMetaData.getTables(null,
                    schema,
                    tableName,
                    new String[] { "TABLE", "VIEW" })) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String limitSelect(int offset, int limit) {
        return "OFFSET "
                + offset
                + " ROWS FETCH NEXT "
                + limit
                + " ROWS ONLY";
    }

    private String getSqlType(ColumnMetadata column) {
        Class<?> fieldType;

        fieldType = column.getField().getType();

        if (fieldType == String.class) {
            Column columnAnnotation;

            columnAnnotation = (Column) column.getAnnotations().get(Column.class);

            if (columnAnnotation == null) {
                return "NVARCHAR(255)";
            } else {
                return "NVARCHAR(" + columnAnnotation.length() + ")";
            }
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BIT";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INT";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "REAL";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "FLOAT";
        } else if (fieldType == BigDecimal.class) {
            return "DECIMAL";
        } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
            return "DATETIME";
        } else if (fieldType == UUID.class) {
            return "UNIQUEIDENTIFIER";
        } else if (column.getField().getType().isEnum()) {
            Enumerated enumeratedAnnotation;

            enumeratedAnnotation = (Enumerated) column.getAnnotations()
                    .get(Enumerated.class);

            if (enumeratedAnnotation == null) {
                return "INT";
            } else {
                if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                    return "INT";
                } else if (enumeratedAnnotation.value() == EnumType.STRING) {
                    return "NVARCHAR(255)";
                } else {
                    throw new RuntimeException("Unsupported enum type.");
                }
            }
        }

        throw new RuntimeException("Unsupported SQL type.");
    }

    @Override
    public PreparedStatement createPersistStatement(Connection connection,
            TableMetadata table) {
        StringBuilder sql;
        StringBuilder output;
        StringBuilder values;
        int index;

        sql = new StringBuilder();

        sql.append("INSERT INTO ");
        sql.append(getTableName(table));
        sql.append(" (");

        output = new StringBuilder();

        values = new StringBuilder();

        index = 1;

        for (ColumnMetadata column : table.getColumns()) {
            GeneratedValue generatedValue;

            generatedValue = (GeneratedValue) column.getAnnotations()
                    .get(GeneratedValue.class);

            if (generatedValue == null) {
                if (index > 1) {
                    sql.append(", ");

                    values.append(", ");
                }

                sql.append(column.getColumnName());

                values.append("?");

                index++;
            } else {
                if (generatedValue.strategy() == GenerationType.SEQUENCE) {
                    if (!output.isEmpty()) {
                        output.append(", ");
                    }

                    output.append("INSERTED.");
                    output.append(column.getColumnName());
                } else if (generatedValue.strategy() == GenerationType.TABLE ||
                        generatedValue.strategy() == GenerationType.UUID) {
                    if (index > 1) {
                        sql.append(", ");

                        values.append(", ");
                    }

                    sql.append(column.getColumnName());

                    values.append("?");

                    index++;
                }
            }
        }

        sql.append(") ");

        if (!output.isEmpty()) {
            sql.append("OUTPUT ");
            sql.append(output);
        }

        sql.append(" VALUES (");
        sql.append(values);
        sql.append(")");

        try {
            return connection.prepareStatement(sql.toString(),
                    Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PreparedStatement createLockStatement(Connection connection, TableMetadata table) {
        try {
            return connection.prepareStatement(
                    "SELECT * FROM " + getTableName(table) + " WITH (TABLOCKX)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
