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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.norm4j.TableGenerator;
import org.norm4j.dialects.SQLDialect;

public class TableIdGenerator {
    private String schema;
    private String table;
    private String pkColumnName;
    private String valueColumnName;
    private int initialValue;

    public TableIdGenerator(TableGenerator tableGenerator) {
        initialValue = 0;

        if (tableGenerator != null) {
            schema = tableGenerator.schema();
            table = tableGenerator.table();
            pkColumnName = tableGenerator.pkColumnName();
            valueColumnName = tableGenerator.valueColumnName();
            initialValue = tableGenerator.initialValue();
        }

        if (schema == null) {
            schema = "";
        }

        if (table == null ||
                table.isEmpty()) {
            table = "norm_sequences";
        }

        if (pkColumnName == null ||
                pkColumnName.isEmpty()) {
            pkColumnName = "sequence_name";
        }

        if (valueColumnName == null ||
                valueColumnName.isEmpty()) {
            valueColumnName = "next_val";
        }
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public Object generateId(Connection connection,
            SQLDialect dialect,
            String sequenceName) {
        boolean exists = false;
        int currentValue;
        int nextValue;
        StringBuilder sql;

        sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append(valueColumnName);
        sql.append(" FROM ");
        sql.append(dialect.getTableName(schema, table));
        sql.append(" WHERE ");
        sql.append(pkColumnName);
        sql.append("=?");

        try {
            try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                ps.setString(1, sequenceName);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentValue = rs.getInt(1);

                        exists = true;
                    } else {
                        currentValue = initialValue;
                    }
                }
            }

            nextValue = currentValue + 1;

            sql = new StringBuilder();

            if (exists) {
                sql.append("UPDATE ");
                sql.append(dialect.getTableName(schema, table));
                sql.append(" SET ");
                sql.append(valueColumnName);
                sql.append("=?");
                sql.append(" WHERE ");
                sql.append(pkColumnName);
                sql.append("=?");
            } else {
                sql.append("INSERT INTO ");
                sql.append(dialect.getTableName(schema, table));
                sql.append(" (");
                sql.append(valueColumnName);
                sql.append(", ");
                sql.append(pkColumnName);
                sql.append(") VALUES (?, ?)");
            }

            try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                ps.setInt(1, nextValue);
                ps.setString(2, sequenceName);

                ps.executeUpdate();
            }

            return nextValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
