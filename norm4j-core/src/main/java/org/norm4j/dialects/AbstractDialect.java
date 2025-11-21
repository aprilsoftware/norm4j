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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.norm4j.EnumType;
import org.norm4j.Enumerated;
import org.norm4j.Join;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.SequenceGenerator;
import org.norm4j.Temporal;
import org.norm4j.TemporalType;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.ForeignKeyMetadata;
import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.Schema;

public abstract class AbstractDialect implements SQLDialect {
    public AbstractDialect() {
    }

    public boolean isArraySupported() {
        return false;
    }

    public boolean isSequenceSupported() {
        return true;
    }

    public boolean isGeneratedKeysForSequenceSupported() {
        return true;
    }

    public boolean isMultiStatementsSupported() {
        return true;
    }

    public List<String> parseMultiStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        int length = sql.length();

        for (int i = 0; i < length; i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < length) ? sql.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote) {
                if (c == '-' && next == '-') {
                    inLineComment = true;
                    i++;
                    continue;
                }

                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            if (!inDoubleQuote && c == '\'') {
                current.append(c);
                if (inSingleQuote) {
                    if (next == '\'') {
                        current.append(next);
                        i++;
                    } else {
                        inSingleQuote = false;
                    }
                } else {
                    inSingleQuote = true;
                }
                continue;
            }

            if (!inSingleQuote && c == '"') {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
                continue;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        String last = current.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }

        return statements;
    }

    public String getTableName(String schema, String tableName) {
        if (schema == null ||
                schema.isEmpty()) {
            return tableName;
        } else {
            return schema
                    + "."
                    + tableName;
        }
    }

    public String getSequenceName(ColumnMetadata column) {
        SequenceGenerator sequenceGenerator;
        String sequenceName;

        sequenceGenerator = (SequenceGenerator) column.getAnnotations()
                .get(SequenceGenerator.class);

        if (sequenceGenerator == null || sequenceGenerator.sequenceName().isEmpty()) {
            sequenceName = createSequenceName(column);
        } else {
            sequenceName = sequenceGenerator.sequenceName();
        }

        if (sequenceGenerator != null &&
                !sequenceGenerator.schema().isEmpty()) {
            return sequenceGenerator.schema()
                    + "."
                    + sequenceName;
        } else {
            return sequenceName;
        }
    }

    public String createSequenceName(ColumnMetadata column) {
        return column.getTable().getTableName()
                + "_"
                + column.getColumnName()
                + "_seq";
    }

    public String createForeignKeyName(TableMetadata table, TableMetadata referenceTable, Join foreignKey) {
        return "fk_"
                + table.getTableName()
                + "_"
                + referenceTable.getTableName();
    }

    public String alterTable(ForeignKeyMetadata foreignKey) {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("ALTER TABLE ");
        ddl.append(getTableName(foreignKey.getTable()));
        ddl.append(" ADD CONSTRAINT ");
        ddl.append(foreignKey.getForeignKeyName());
        ddl.append(" FOREIGN KEY (");

        for (int i = 0; i < foreignKey.getJoin().columns().length; i++) {
            if (i > 0) {
                ddl.append(", ");
            }

            ddl.append(foreignKey.getJoin().columns()[i]);
        }

        ddl.append(") REFERENCES ");
        ddl.append(getTableName(foreignKey.getReferenceTable().getSchema(),
                foreignKey.getReferenceTable().getTableName()));
        ddl.append(" (");

        for (int i = 0; i < foreignKey.getJoin().reference().columns().length; i++) {
            String columnName;

            columnName = foreignKey.getJoin().reference().columns()[i];

            if (i > 0) {
                ddl.append(", ");
            }

            ddl.append(columnName);
        }

        ddl.append(")");

        if (foreignKey.getJoin().cascadeDelete()) {
            ddl.append(" ON DELETE CASCADE");
        }

        return ddl.toString();
    }

    public String alterTable(String tableSchema, String tableName, Schema.ForeignKey foreignKey) {
        StringBuilder ddl;

        ddl = new StringBuilder();

        ddl.append("ALTER TABLE ");
        ddl.append(getTableName(tableSchema, tableName));
        ddl.append(" ADD CONSTRAINT ");
        ddl.append(foreignKey.getName());
        ddl.append(" FOREIGN KEY (");

        for (int i = 0; i < foreignKey.getColumns().size(); i++) {
            if (i > 0) {
                ddl.append(", ");
            }

            ddl.append(foreignKey.getColumns().get(i));
        }

        ddl.append(") REFERENCES ");
        ddl.append(getTableName(foreignKey.getReferenceSchema(),
                foreignKey.getReferenceTable()));
        ddl.append(" (");

        for (int i = 0; i < foreignKey.getReferenceColumns().size(); i++) {
            String columnName;

            columnName = foreignKey.getReferenceColumns().get(i);

            if (i > 0) {
                ddl.append(", ");
            }

            ddl.append(columnName);
        }

        ddl.append(")");

        if (foreignKey.isCascadeDelete()) {
            ddl.append(" ON DELETE CASCADE");
        }

        return ddl.toString();
    }

    public PreparedStatement createPersistStatement(Connection connection,
            TableMetadata table) {
        StringBuilder sql;
        StringBuilder values;
        int index;

        sql = new StringBuilder();

        sql.append("INSERT INTO ");
        sql.append(getTableName(table));
        sql.append(" (");

        values = new StringBuilder();

        index = 1;

        for (ColumnMetadata column : table.getColumns()) {
            GeneratedValue generatedValue;

            generatedValue = (GeneratedValue) column.getAnnotations()
                    .get(GeneratedValue.class);

            if (generatedValue == null ||
                    (generatedValue != null &&
                            generatedValue.strategy() != GenerationType.AUTO &&
                            generatedValue.strategy() != GenerationType.IDENTITY &&
                            generatedValue.strategy() != GenerationType.SEQUENCE)) {
                if (index > 1) {
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

        try {
            return connection.prepareStatement(sql.toString(),
                    Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement createLockStatement(Connection connection, TableMetadata table) {
        try {
            return connection.prepareStatement(
                    "SELECT * FROM " + getTableName(table) + " FOR UPDATE");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object fromSqlValue(ColumnMetadata column, Object value) {
        if (value != null) {
            if (column.getField().getType().isEnum()) {
                Enumerated enumerated;

                enumerated = (Enumerated) column.getAnnotations()
                        .get(Enumerated.class);

                if (enumerated == null || enumerated.value() == EnumType.ORDINAL) {
                    value = column.getField().getType().getEnumConstants()[(int) value];
                } else {
                    value = Enum.valueOf(column.getField().getType()
                            .asSubclass(Enum.class), (String) value);
                }
            } else if (column.getField().getType() == UUID.class
                    && value instanceof String) {
                value = UUID.fromString((String) value);
            } else if (column.getField().getType() == java.sql.Date.class &&
                    value instanceof java.sql.Time) {
                value = truncateTime(column,
                        new java.sql.Date(((java.sql.Time) value).getTime()));
            } else if (column.getField().getType() == java.sql.Date.class &&
                    value instanceof java.sql.Timestamp) {
                value = truncateTime(column,
                        new java.sql.Date(((java.sql.Timestamp) value).getTime()));
            } else if (value instanceof java.sql.Date ||
                    value instanceof java.util.Date) {
                value = truncateTime(column, value);
            }
        }

        return value;
    }

    public Object toSqlValue(ColumnMetadata column, Object value) {
        if (value != null) {
            if (column.getField().getType().isEnum()) {
                Enumerated enumerated;

                enumerated = (Enumerated) column.getAnnotations()
                        .get(Enumerated.class);

                if (enumerated == null || enumerated.value() == EnumType.ORDINAL) {
                    value = ((Enum<?>) value).ordinal();
                } else {
                    value = ((Enum<?>) value).name();
                }
            } else if (value instanceof java.sql.Date ||
                    value instanceof java.util.Date) {
                value = truncateTime(column, value);
            }
        }

        return value;
    }

    private Object truncateTime(ColumnMetadata column, Object value) {
        Temporal temporal;

        temporal = (Temporal) column.getAnnotations()
                .get(Temporal.class);

        if (temporal != null &&
                temporal.value() == TemporalType.DATE) {
            Calendar calendar;

            calendar = Calendar.getInstance();

            if (value instanceof java.sql.Date) {
                calendar.setTime((java.sql.Date) value);
            } else {
                calendar.setTime((java.util.Date) value);
            }

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (value instanceof java.sql.Date) {
                value = new java.sql.Date(calendar.getTime().getTime());
            } else {
                value = calendar.getTime();
            }
        }

        return value;
    }
}
