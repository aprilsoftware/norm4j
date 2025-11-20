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
import java.util.List;
import java.util.ServiceLoader;

import org.norm4j.Join;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.ForeignKeyMetadata;
import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.Schema;

public interface SQLDialect {
        public boolean isDialect(String productName);

        public boolean isTupleSupported();

        public boolean isArraySupported();

        public boolean isSequenceSupported();

        public boolean isGeneratedKeysForSequenceSupported();

        public boolean isMultiStatementsSupported();

        public List<String> parseMultiStatements(String sql);

        public String createForeignKeyName(TableMetadata table,
                        TableMetadata referenceTable,
                        Join foreignKey);

        public default String getTableName(TableMetadata table) {
                return getTableName(table.getSchema(), table.getTableName());
        }

        public String getTableName(String schema, String tableName);

        public String getSequenceName(ColumnMetadata column);

        public String createSequenceName(ColumnMetadata column);

        public String createSequence(String schema,
                        String sequenceName,
                        int initialValue);

        public String createTable(TableMetadata table);

        public String createSequenceTable(String schema,
                        String tableName,
                        String pkColumnName,
                        String valueColumnName);

        public String alterTable(ForeignKeyMetadata foreignKey);

        public boolean sequenceExists(Connection connection,
                        String schema,
                        String sequenceName);

        public boolean tableExists(Connection connection,
                        String schema,
                        String tableName);

        public PreparedStatement createPersistStatement(Connection connection,
                        TableMetadata table);

        public PreparedStatement createLockStatement(Connection connection, TableMetadata table);

        public Object fromSqlValue(ColumnMetadata column, Object value);

        public Object toSqlValue(ColumnMetadata column, Object value);

        public String limitSelect(int offset, int limit);

        public String createTable(Schema.Table table);

        public String addColumn(String tableSchema, String tableName, Schema.Column column);

        public String addForeignKey(String tableSchema, String tableName, Schema.ForeignKey foreignKey);

        public String createSequence(Schema.Sequence sequence);

        public static SQLDialect detectDialect(Connection connection) {
                String productName;

                try {
                        productName = connection.getMetaData()
                                        .getDatabaseProductName().toLowerCase();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }

                return getDialectByProductName(productName);
        }

        public static SQLDialect getDialectByProductName(String productName) {
                for (SQLDialect dialect : ServiceLoader.load(SQLDialect.class)) {
                        if (dialect.isDialect(productName)) {
                                return dialect;
                        }
                }

                throw new RuntimeException("No supported dialect for the product name.");
        }
}
