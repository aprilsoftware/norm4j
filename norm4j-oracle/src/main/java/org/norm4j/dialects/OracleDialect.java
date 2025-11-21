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
import org.norm4j.schema.Schema;

public class OracleDialect extends AbstractDialect {
    public OracleDialect() {
    }

    public boolean isDialect(String productName) {
        return productName.toLowerCase()
                .contains("oracle");
    }

    public boolean isTupleSupported() {
        return true;
    }

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
                if (generatedValueAnnotation.strategy() == GenerationType.SEQUENCE) {
                    ddl.append(" DEFAULT ");
                    ddl.append(getSequenceName(column));
                    ddl.append(".NEXTVAL");
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

    public String createSequenceTable(String schema,
            String tableName,
            String pkColumnName,
            String valueColumnName) {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(schema, tableName));
        ddl.append(" (");
        ddl.append(pkColumnName);
        ddl.append(" VARCHAR2(255) NOT NULL, ");
        ddl.append(valueColumnName);
        ddl.append(" NUMBER NOT NULL, PRIMARY KEY (");
        ddl.append(pkColumnName);
        ddl.append("));");

        return ddl.toString();
    }

    public boolean sequenceExists(Connection connection,
            String schema,
            String sequenceName) {
        return exists(connection,
                schema,
                sequenceName,
                new String[] { "SEQUENCE" });
    }

    public boolean tableExists(Connection connection,
            String schema,
            String tableName) {
        return exists(connection,
                schema,
                tableName,
                new String[] { "TABLE", "VIEW" });
    }

    private boolean exists(Connection connection,
            String schema,
            String objectName,
            String[] objectTypes) {
        DatabaseMetaData dbMetaData;

        if (schema != null && schema.isEmpty()) {
            schema = null;
        }

        try {
            dbMetaData = connection.getMetaData();

            try (ResultSet rs = dbMetaData.getTables(null,
                    schema,
                    objectName,
                    objectTypes)) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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
                return "VARCHAR2(255)";
            } else {
                return "VARCHAR2(" + columnAnnotation.length() + ")";
            }
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "NUMBER(1)";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "NUMBER(10)";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "NUMBER(19)";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "BINARY_FLOAT";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "BINARY_DOUBLE";
        } else if (fieldType == BigDecimal.class) {
            return "NUMBER";
        } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
            return "TIMESTAMP";
        } else if (fieldType == UUID.class) {
            return "RAW(16)";
        } else if (column.getField().getType().isEnum()) {
            Enumerated enumeratedAnnotation;

            enumeratedAnnotation = (Enumerated) column.getAnnotations()
                    .get(Enumerated.class);

            if (enumeratedAnnotation == null) {
                return "NUMBER(10)";
            } else {
                if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                    return "NUMBER(10)";
                } else if (enumeratedAnnotation.value() == EnumType.STRING) {
                    return "VARCHAR2(255)";
                } else {
                    throw new RuntimeException("Unsupported enum type.");
                }
            }
        }

        throw new RuntimeException("Unsupported SQL type.");
    }

    public PreparedStatement createPersistStatement(Connection connection,
            TableMetadata table) {
        List<String> generatedKeys;
        StringBuilder sql;
        StringBuilder values;
        int index;

        sql = new StringBuilder();

        sql.append("INSERT INTO ");
        sql.append(getTableName(table));
        sql.append(" (");

        values = new StringBuilder();

        index = 1;

        generatedKeys = new ArrayList<>();

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
                    if (index > 1) {
                        sql.append(", ");

                        values.append(", ");
                    }

                    sql.append(column.getColumnName());

                    values.append(getSequenceName(column));
                    values.append(".NEXTVAL");

                    generatedKeys.add(column.getColumnName());

                    index++;
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

        sql.append(") VALUES (");
        sql.append(values);
        sql.append(")");

        try {
            return connection.prepareStatement(sql.toString(),
                    generatedKeys.toArray(new String[generatedKeys.size()]));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String createTable(Schema.Table table) {
        return null;
    }

    public String alterTable(String tableSchema, String tableName, Schema.Column column) {
        return null;
    }

    public String createSequence(Schema.Sequence sequence) {
        return null;
    }
}
