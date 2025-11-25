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
package org.norm4j.metadata.helpers;

import org.norm4j.dialects.SQLDialect;
import org.norm4j.metadata.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableCreationHelper {

    private final Map<Class<?>, TableMetadata> metadataMap;
    private final DdlHelper ddlHelper;
    private final SqlExecutor sqlExecutor;

    public TableCreationHelper(Map<Class<?>, TableMetadata> metadataMap, SqlExecutor sqlExecutor) {
        this.metadataMap = metadataMap;
        this.sqlExecutor = sqlExecutor;
        this.ddlHelper = new DdlHelper(metadataMap);
    }

    public List<TableMetadata> getExistingTables(Connection connection, SQLDialect dialect) throws SQLException {
        List<TableMetadata> existingTables = new ArrayList<>();

        for (TableMetadata tableMetadata : metadataMap.values()) {
            if (dialect.tableExists(connection, tableMetadata.getSchema(), tableMetadata.getTableName())) {
                existingTables.add(tableMetadata);
            }
        }

        return existingTables;
    }

    public void createSequenceTables(Connection connection, SQLDialect dialect) throws SQLException {
        for (String sql : ddlHelper.createSequenceTables(dialect)) {
            sqlExecutor.execute(connection, sql);
        }
    }

    public void createTablesAndSequences(Connection connection, SQLDialect dialect,
            List<TableMetadata> existingTables) throws SQLException {
        for (TableMetadata tableMetadata : metadataMap.values()) {
            if (ddlHelper.tableExists(tableMetadata, existingTables)) {
                continue;
            }

            createSequencesForTable(connection, dialect, tableMetadata);
            sqlExecutor.execute(connection, dialect.createTable(tableMetadata));
        }
    }

    public void createSequencesForTable(Connection connection, SQLDialect dialect,
            TableMetadata tableMetadata) throws SQLException {
        for (String sql : ddlHelper.createSequences(tableMetadata, dialect)) {
            sqlExecutor.execute(connection, sql);
        }
    }

    public void addForeignKeyConstraints(Connection connection, SQLDialect dialect,
            List<TableMetadata> existingTables) throws SQLException {
        for (String sql : ddlHelper.createForeignKeys(dialect, existingTables)) {
            sqlExecutor.execute(connection, sql);
        }
    }
}
