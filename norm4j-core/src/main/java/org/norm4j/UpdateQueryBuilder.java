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

import java.sql.Connection;

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public class UpdateQueryBuilder extends QueryBuilder<UpdateQueryBuilder> {
    private final StringBuilder tableClause;
    private final StringBuilder setClause;

    public UpdateQueryBuilder(TableManager tableManager) {
        super(tableManager);

        tableClause = new StringBuilder();

        setClause = new StringBuilder();
    }

    protected UpdateQueryBuilder self() {
        return this;
    }

    public UpdateQueryBuilder update(Class<?> tableClass) {
        TableMetadata table;

        if (!tableClause.isEmpty()) {
            throw new RuntimeException("update(...) must be called only once.");
        }

        table = getTable(tableClass);

        tableClause.append(getTableManager().getDialect()
                .getTableName(table));

        return this;
    }

    public <T, R> UpdateQueryBuilder set(FieldGetter<T, R> fieldGetter, Object value) {
        ColumnMetadata column;

        column = getTableManager()
                .getMetadataManager()
                .getMetadata(fieldGetter);

        if (!setClause.isEmpty()) {
            setClause.append(", ");
        }

        setClause.append(column.getColumnName());

        setClause.append(" = ");

        appendValue(value, setClause, column);

        return this;
    }

    public <T, R> UpdateQueryBuilder set(FieldGetter<T, R> fieldGetter, SelectQueryBuilder builder) {
        ColumnMetadata column;

        column = getTableManager()
                .getMetadataManager()
                .getMetadata(fieldGetter);

        if (!setClause.isEmpty()) {
            setClause.append(", ");
        }

        setClause.append(column.getColumnName());

        setClause.append(" = ");

        setClause.append(builder.build());

        return this;
    }

    public String build() {
        StringBuilder statement;

        statement = new StringBuilder();

        statement.append("UPDATE ");
        statement.append(tableClause.toString());
        statement.append(" SET ");
        statement.append(setClause.toString());

        if (!getWhereClause().isEmpty()) {
            statement.append(getWhereClause().toString());
        }

        return statement.toString();
    }

    public int executeUpdate() {
        return executeUpdate(null);
    }

    public int executeUpdate(Connection connection) {
        Query query;

        query = getTableManager().createQuery(build());

        for (int i = 0; i < getParameters().size(); i++) {
            query.setParameter(i + 1, getParameters().get(i));
        }

        if (connection == null) {
            return query.executeUpdate();
        } else {
            return query.executeUpdate(connection);
        }
    }
}
