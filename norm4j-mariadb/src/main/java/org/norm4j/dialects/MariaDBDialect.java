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

import org.norm4j.Column;
import org.norm4j.EnumType;
import org.norm4j.Enumerated;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.SchemaColumn;
import org.norm4j.schema.SchemaTable;
import org.norm4j.schema.annotations.Annotation;
import org.norm4j.schema.annotations.ColumnAnnotation;
import org.norm4j.schema.annotations.EnumeratedAnnotation;
import org.norm4j.schema.annotations.GeneratedValueAnnotation;
import org.norm4j.schema.annotations.IdAnnotation;

public class MariaDBDialect extends AbstractDialect {
    public MariaDBDialect() {
    }

    @Override
    public boolean isDialect(String productName) {
        productName = productName.toLowerCase();

        return productName.contains("mariadb") ||
                productName.contains("mysql");
    }

    @Override
    public boolean isTupleSupported() {
        return true;
    }

    @Override
    public boolean isSequenceSupported() {
        return false;
    }

    @Override
    public boolean isGeneratedKeysForSequenceSupported() {
        return false;
    }

    @Override
    public boolean isMultiStatementsSupported() {
        return false;
    }

    @Override
    public String getTableName(String schema, String tableName) {
        if (schema == null ||
                schema.isEmpty()) {
            return tableName;
        } else {
            return schema
                    + "_"
                    + tableName;
        }
    }

    @Override
    public String getSequenceName(ColumnMetadata column) {
        throw new UnsupportedOperationException("MariaDB does not support sequences.");
    }

    @Override
    public String createSequenceName(ColumnMetadata column) {
        throw new UnsupportedOperationException("MariaDB does not support sequences.");
    }

    @Override
    public String createSequence(String schema, String sequenceName, int initialValue) {
        throw new UnsupportedOperationException("MariaDB does not support sequences.");
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

            generatedValueAnnotation = (GeneratedValue) column
                    .getAnnotations().get(GeneratedValue.class);

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
                    ddl.append(" AUTO_INCREMENT");
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

        ddl.append(") ENGINE=InnoDB;");

        return ddl.toString();
    }

    @Override
    public String createTable(SchemaTable table) {
        List<String> primaryKeys;
        List<SchemaColumn> columns;
        StringBuilder ddl;

        columns = table.getColumns();

        ddl = new StringBuilder();

        ddl.append("CREATE TABLE ");
        ddl.append(getTableName(table));
        ddl.append(" (");

        primaryKeys = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            GeneratedValueAnnotation generatedValueAnnotation;
            ColumnAnnotation columnAnnotation;
            SchemaColumn column;
            String columnName;

            column = columns.get(i);

            columnAnnotation = Annotation.get(column, ColumnAnnotation.class);

            generatedValueAnnotation = Annotation.get(column, GeneratedValueAnnotation.class);

            if (i > 0) {
                ddl.append(", ");
            }

            columnName = column.getColumnName(columnAnnotation);

            ddl.append(columnName);
            ddl.append(" ");

            if (columnAnnotation == null ||
                    columnAnnotation.getColumnDefinition().isEmpty()) {
                ddl.append(getSqlType(column));
            } else {
                ddl.append(columnAnnotation.getColumnDefinition());
            }

            Class<?> fieldType;

            fieldType = column.getFieldTypeClass();

            IdAnnotation idAnnotation;

            idAnnotation = Annotation.get(column, IdAnnotation.class);

            if (idAnnotation != null) {
                primaryKeys.add(columnName);
            }

            if (generatedValueAnnotation == null) {
                if (fieldType.isPrimitive() ||
                        (columnAnnotation != null && !columnAnnotation.isNullable()) ||
                        idAnnotation != null) {
                    ddl.append(" NOT NULL");
                }
            } else {
                if (generatedValueAnnotation.getStrategy() == GenerationType.AUTO ||
                        generatedValueAnnotation.getStrategy() == GenerationType.IDENTITY) {
                    ddl.append(" AUTO_INCREMENT");
                }
            }
        }

        if (!primaryKeys.isEmpty()) {
            ddl.append(", PRIMARY KEY (");

            for (int i = 0; i < primaryKeys.size(); i++) {
                if (i > 0) {
                    ddl.append(", ");
                }

                ddl.append(primaryKeys.get(i));
            }

            ddl.append(")");
        }

        ddl.append(") ENGINE=InnoDB;");

        return ddl.toString();
    }

    @Override
    public String alterTableAddColumn(SchemaTable table, SchemaColumn column) {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("ALTER TABLE ");
        ddl.append(getTableName(table.getSchema(), table.getTableName()));
        ddl.append(" ADD COLUMN ");

        ColumnAnnotation columnAnnotation;

        columnAnnotation = Annotation.get(column, ColumnAnnotation.class);

        ddl.append(column.getColumnName(columnAnnotation));
        ddl.append(" ");

        if (columnAnnotation == null ||
                columnAnnotation.getColumnDefinition().isEmpty()) {
            ddl.append(getSqlType(column));
        } else {
            ddl.append(columnAnnotation.getColumnDefinition());
        }

        ddl.append(";");

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
        ddl.append(" VARCHAR(255) NOT NULL, ");
        ddl.append(valueColumnName);
        ddl.append(" BIGINT NOT NULL, PRIMARY KEY (");
        ddl.append(pkColumnName);
        ddl.append("));");

        return ddl.toString();
    }

    public boolean sequenceExists(Connection connection,
            String schema,
            String sequenceName) {
        throw new UnsupportedOperationException("MariaDB does not support sequences.");
    }

    public boolean tableExists(Connection connection,
            String schema,
            String tableName) {
        DatabaseMetaData dbMetaData;

        try {
            dbMetaData = connection.getMetaData();

            try (ResultSet rs = dbMetaData.getTables(null,
                    null,
                    getTableName(schema, tableName),
                    new String[] { "TABLE", "VIEW" })) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String limitSelect(int offset, int limit) {
        return "LIMIT "
                + limit
                + " OFFSET "
                + offset;
    }

    private String getSqlType(ColumnMetadata column) {
        Class<?> fieldType;

        fieldType = column.getField().getType();

        if (fieldType == String.class) {
            Column columnAnnotation;

            columnAnnotation = (Column) column.getAnnotations().get(Column.class);

            if (columnAnnotation == null) {
                return "VARCHAR(255)";
            } else {
                return "VARCHAR(" + columnAnnotation.length() + ")";
            }
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INT";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "FLOAT";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "DOUBLE";
        } else if (fieldType == BigDecimal.class) {
            return "DECIMAL";
        } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
            return "DATETIME";
        } else if (fieldType == UUID.class) {
            return "CHAR(36)";
        } else if (column.getField().getType().isEnum()) {
            Enumerated enumeratedAnnotation;

            enumeratedAnnotation = (Enumerated) column.getAnnotations().get(Enumerated.class);

            if (enumeratedAnnotation == null) {
                return "INT";
            } else {
                if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                    return "INT";
                } else if (enumeratedAnnotation.value() == EnumType.STRING) {
                    return "VARCHAR(255)";
                } else {
                    throw new RuntimeException("Unsupported enum type.");
                }
            }
        }

        throw new RuntimeException("Unsupported SQL type.");
    }

    private String getSqlType(SchemaColumn column) {
        Class<?> fieldType;

        fieldType = column.getFieldTypeClass();

        if (fieldType == String.class) {
            ColumnAnnotation columnAnnotation;

            columnAnnotation = Annotation.get(column, ColumnAnnotation.class);

            if (columnAnnotation == null) {
                return "VARCHAR(255)";
            } else {
                return "VARCHAR(" + columnAnnotation.getLength() + ")";
            }
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INT";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "FLOAT";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "DOUBLE";
        } else if (fieldType == BigDecimal.class) {
            return "DECIMAL";
        } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
            return "DATETIME";
        } else if (fieldType == UUID.class) {
            return "CHAR(36)";
        } else if (fieldType.isEnum()) {
            EnumeratedAnnotation enumeratedAnnotation;

            enumeratedAnnotation = Annotation.get(column, EnumeratedAnnotation.class);

            if (enumeratedAnnotation == null) {
                return "INT";
            } else {
                if (enumeratedAnnotation.getValue() == EnumType.ORDINAL) {
                    return "INT";
                } else if (enumeratedAnnotation.getValue() == EnumType.STRING) {
                    return "VARCHAR(255)";
                } else {
                    throw new RuntimeException("Unsupported enum type.");
                }
            }
        }

        throw new RuntimeException("Unsupported SQL type.");
    }
}