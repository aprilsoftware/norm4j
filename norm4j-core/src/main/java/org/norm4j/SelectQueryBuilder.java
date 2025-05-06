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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public class SelectQueryBuilder extends QueryBuilder<SelectQueryBuilder> {
    private final List<FromClauseTable> fromClauseTables;
    private final StringBuilder selectClause;
    private final StringBuilder fromClause;
    private final StringBuilder orderByClause;
    private final StringBuilder groupByClause;
    private int offset;
    private int limit;

    public SelectQueryBuilder(TableManager tableManager) {
        super(tableManager);

        fromClauseTables = new ArrayList<>();

        selectClause = new StringBuilder();

        fromClause = new StringBuilder();

        orderByClause = new StringBuilder();

        groupByClause = new StringBuilder();
    }

    protected SelectQueryBuilder self() {
        return this;
    }

    public SelectQueryBuilder count() {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append("count(*)");

        return this;
    }

    public <T, R> SelectQueryBuilder count(FieldGetter<T, R> fieldGetter) {
        return count(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder count(FieldGetter<T, R> fieldGetter, String alias) {
        appendAggregateFunction(fieldGetter, alias, "count");

        return this;
    }

    public <T, R> SelectQueryBuilder count(SelectQueryBuilder builder) {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append("count(");
        selectClause.append(builder.build());
        selectClause.append(")");

        return this;
    }

    public <T, R> SelectQueryBuilder sum(FieldGetter<T, R> fieldGetter) {
        return sum(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder sum(FieldGetter<T, R> fieldGetter, String alias) {
        appendAggregateFunction(fieldGetter, alias, "sum");

        return this;
    }

    public <T, R> SelectQueryBuilder avg(FieldGetter<T, R> fieldGetter) {
        return avg(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder avg(FieldGetter<T, R> fieldGetter, String alias) {
        appendAggregateFunction(fieldGetter, alias, "avg");

        return this;
    }

    public <T, R> SelectQueryBuilder min(FieldGetter<T, R> fieldGetter) {
        return min(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder min(FieldGetter<T, R> fieldGetter, String alias) {
        appendAggregateFunction(fieldGetter, alias, "min");

        return this;
    }

    public <T, R> SelectQueryBuilder max(FieldGetter<T, R> fieldGetter) {
        return max(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder max(FieldGetter<T, R> fieldGetter, String alias) {
        appendAggregateFunction(fieldGetter, alias, "max");

        return this;
    }

    private <T, R> void appendAggregateFunction(FieldGetter<T, R> fieldGetter,
            String alias,
            String aggregateFunction) {
        ColumnMetadata column;

        column = getTableManager().getMetadataManager()
                .getMetadata(fieldGetter);

        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append(aggregateFunction);
        selectClause.append("(");

        append(column, alias, selectClause);

        selectClause.append(")");
    }

    public SelectQueryBuilder select() {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append("*");

        return this;
    }

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter) {
        return select(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter, String alias) {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        append(fieldGetter, alias, selectClause);

        return this;
    }

    public SelectQueryBuilder select(Class<?> tableClass) {
        return select(tableClass, null);
    }

    public SelectQueryBuilder select(Class<?> tableClass, String alias) {
        TableMetadata table;

        table = getTable(tableClass);

        for (ColumnMetadata column : table.getColumns()) {
            if (!selectClause.isEmpty()) {
                selectClause.append(", ");
            }

            append(column, alias, selectClause);
        }

        return this;
    }

    public SelectQueryBuilder select(SelectQueryBuilder builder) {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append("(");
        selectClause.append(builder.build());
        selectClause.append(")");

        getParameters().addAll(builder.getParameters());

        return this;
    }

    public SelectQueryBuilder select(String expression) {
        if (!selectClause.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append(expression);

        return this;
    }

    public SelectQueryBuilder from(Class<?> tableClass) {
        return from(tableClass, null);
    }

    public SelectQueryBuilder from(Class<?> tableClass, String alias) {
        TableMetadata table;

        if (!fromClause.isEmpty()) {
            throw new RuntimeException("from(...) must be called only once.");
        }

        table = getTable(tableClass);

        fromClause.append(getTableManager().getDialect()
                .getTableName(table));

        if (alias != null) {
            fromClause.append(" AS ");
            fromClause.append(alias);
        }

        fromClauseTables.add(new FromClauseTable(table, alias));

        return this;
    }

    public SelectQueryBuilder from(SelectQueryBuilder builder, String alias) {
        if (!fromClause.isEmpty()) {
            throw new RuntimeException("from(...) must be called only once.");
        }

        fromClause.append("(");
        fromClause.append(builder.build());
        fromClause.append(")");
        fromClause.append(" AS ");
        fromClause.append(alias);

        getParameters().addAll(builder.getParameters());

        return this;
    }

    public SelectQueryBuilder from(String expression) {
        return from(expression, null, null);
    }

    public SelectQueryBuilder from(String expression, List<Object> parameters) {
        return from(expression, null, parameters);
    }

    public SelectQueryBuilder from(String expression, String alias, List<Object> parameters) {
        if (!fromClause.isEmpty()) {
            throw new RuntimeException("from(...) must be called only once.");
        }

        fromClause.append(expression);

        if (alias != null) {
            fromClause.append(" AS ");
            fromClause.append(alias);
        }

        if (parameters != null) {
            getParameters().addAll(parameters);
        }

        return this;
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder innerJoin(Class<?> tableClass,
            FieldGetter<T, R>... fieldGetters) {
        return innerJoin(tableClass, null, fieldGetters);
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder innerJoin(Class<?> tableClass,
            String alias,
            FieldGetter<T, R>... fieldGetters) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before innerJoin(...).");
        }

        join(tableClass, alias, "INNER JOIN", fieldGetters);

        return this;
    }

    public SelectQueryBuilder innerJoin(SelectQueryBuilder builder,
            String alias,
            String joinExpression) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before innerJoin(...).");
        }

        join(builder, alias, joinExpression, "INNER JOIN");

        return this;
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder leftJoin(Class<?> tableClass,
            FieldGetter<T, R>... fieldGetters) {
        return leftJoin(tableClass, null, fieldGetters);
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder leftJoin(Class<?> tableClass,
            String alias,
            FieldGetter<T, R>... fieldGetters) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before leftJoin(...).");
        }

        join(tableClass, alias, "LEFT JOIN", fieldGetters);

        return this;
    }

    public SelectQueryBuilder leftJoin(SelectQueryBuilder builder,
            String alias,
            String joinExpression) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before leftJoin(...).");
        }

        join(builder, alias, joinExpression, "LEFT JOIN");

        return this;
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder rightJoin(Class<?> tableClass,
            FieldGetter<T, R>... fieldGetters) {
        return rightJoin(tableClass, null, fieldGetters);
    }

    @SafeVarargs
    public final <T, R> SelectQueryBuilder rightJoin(Class<?> tableClass,
            String alias,
            FieldGetter<T, R>... fieldGetters) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before rightJoin(...).");
        }

        join(tableClass, alias, "RIGHT JOIN", fieldGetters);

        return this;
    }

    public SelectQueryBuilder rightJoin(SelectQueryBuilder builder,
            String alias,
            String joinExpression) {
        if (fromClause.isEmpty()) {
            throw new RuntimeException("Call from(...) before rightJoin(...).");
        }

        join(builder, alias, joinExpression, "RIGHT JOIN");

        return this;
    }

    private <T, R> void join(Class<?> tableClass,
            String alias,
            String joinType,
            FieldGetter<T, R>... fieldGetters) {
        TableMetadata table;

        table = getTable(tableClass);

        fromClause.append(" ");
        fromClause.append(joinType);
        fromClause.append(" ");
        fromClause.append(getTableManager().getDialect()
                .getTableName(table));

        if (alias != null) {
            fromClause.append(" AS ");
            fromClause.append(alias);
        }

        fromClause.append(" ON ");
        fromClause.append(getJoinExpression(table, alias, fieldGetters));

        fromClauseTables.add(new FromClauseTable(table, alias));
    }

    private <T, R> String getJoinExpression(TableMetadata table, String alias, FieldGetter<T, R>... fieldGetters) {
        for (Join join : table.getJoins()) {
            if (fieldGetters.length > 0) {
                if (!getTableManager().getMetadataManager().compareColumns(table, join, fieldGetters)) {
                    continue;
                }
            }

            for (FromClauseTable fromClauseTable : fromClauseTables) {
                if (join.reference().table().equals(fromClauseTable.table.getTableClass())) {
                    StringBuilder expression;

                    expression = new StringBuilder();

                    for (int i = 0; i < join.columns().length; i++) {
                        ColumnMetadata leftColumn;
                        ColumnMetadata rightColumn;
                        String leftColumnNane;
                        String rightColumnName;

                        leftColumnNane = join.columns()[i];
                        rightColumnName = join.reference().columns()[i];

                        leftColumn = table.getColumns().stream()
                                .filter(c -> c.getColumnName()
                                        .equals(leftColumnNane))
                                .findFirst().get();

                        rightColumn = fromClauseTable.table.getColumns().stream()
                                .filter(c -> c.getColumnName()
                                        .equals(rightColumnName))
                                .findFirst().get();

                        if (!expression.isEmpty()) {
                            expression.append(" AND ");
                        }

                        if (alias == null) {
                            expression.append(getTableManager().getDialect()
                                    .getTableName(table));
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        } else {
                            expression.append(alias);
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }

                        expression.append(" = ");

                        if (fromClauseTable.alias == null) {
                            expression.append(getTableManager().getDialect()
                                    .getTableName(fromClauseTable.table));
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        } else {
                            expression.append(fromClauseTable.alias);
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        }
                    }

                    return expression.toString();
                }
            }
        }

        for (FromClauseTable fromClauseTable : fromClauseTables) {
            for (Join join : fromClauseTable.table.getJoins()) {
                if (join.reference().table().equals(table.getTableClass())) {
                    StringBuilder expression;

                    if (fieldGetters.length > 0) {
                        if (!getTableManager().getMetadataManager().compareColumns(fromClauseTable.table,
                                join,
                                fieldGetters)) {
                            continue;
                        }
                    }

                    expression = new StringBuilder();

                    for (int i = 0; i < join.columns().length; i++) {
                        ColumnMetadata leftColumn;
                        ColumnMetadata rightColumn;
                        String leftColumnName;
                        String rightColumnName;

                        leftColumnName = join.reference().columns()[i];
                        rightColumnName = join.columns()[i];

                        leftColumn = table.getColumns().stream()
                                .filter(c -> c.getColumnName()
                                        .equals(leftColumnName))
                                .findFirst().get();

                        rightColumn = fromClauseTable.table.getColumns().stream()
                                .filter(c -> c.getColumnName()
                                        .equals(rightColumnName))
                                .findFirst().get();

                        if (!expression.isEmpty()) {
                            expression.append(" AND ");
                        }

                        if (alias == null) {
                            expression.append(getTableManager().getDialect()
                                    .getTableName(table));
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        } else {
                            expression.append(alias);
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }

                        expression.append(" = ");

                        if (fromClauseTable.alias == null) {
                            expression.append(getTableManager().getDialect()
                                    .getTableName(fromClauseTable.table));
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        } else {
                            expression.append(fromClauseTable.alias);
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        }
                    }

                    return expression.toString();
                }
            }
        }

        throw new RuntimeException("Missing join for table "
                + table.getTableName()
                + ".");
    }

    private void join(SelectQueryBuilder builder,
            String alias,
            String joinExpression,
            String joinType) {
        fromClause.append(" ");
        fromClause.append(joinType);
        fromClause.append(" (");
        fromClause.append(builder.build());
        fromClause.append(") AS ");
        fromClause.append(alias);
        fromClause.append(" ON ");
        fromClause.append(joinExpression);

        getParameters().addAll(builder.getParameters());
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter) {
        return orderBy(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter, String alias) {
        if (!orderByClause.isEmpty()) {
            orderByClause.append(", ");
        }

        append(fieldGetter, alias, orderByClause);

        return this;
    }

    public <T, R> SelectQueryBuilder orderByDesc(FieldGetter<T, R> fieldGetter) {
        return orderByDesc(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder orderByDesc(FieldGetter<T, R> fieldGetter, String alias) {
        orderBy(fieldGetter, alias);

        orderByClause.append(" DESC");

        return this;
    }

    public SelectQueryBuilder orderBy(SelectQueryBuilder builder) {
        if (!orderByClause.isEmpty()) {
            orderByClause.append(", ");
        }

        orderByClause.append("(");
        orderByClause.append(builder.build());
        orderByClause.append(")");

        getParameters().addAll(builder.getParameters());

        return this;
    }

    public <T, R> SelectQueryBuilder orderByDesc(SelectQueryBuilder builder) {
        orderBy(builder);

        orderByClause.append(" DESC");

        return this;
    }

    public SelectQueryBuilder orderBy(String expression) {
        return orderBy(expression, null);
    }

    public SelectQueryBuilder orderBy(String expression,
            List<Object> expressionParameters) {
        if (!orderByClause.isEmpty()) {
            orderByClause.append(", ");
        }

        orderByClause.append(expression);

        if (expressionParameters != null) {
            getParameters().addAll(expressionParameters);
        }

        return this;
    }

    public <T, R> SelectQueryBuilder groupBy(FieldGetter<T, R> fieldGetter) {
        return groupBy(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder groupBy(FieldGetter<T, R> fieldGetter,
            String alias) {
        if (!groupByClause.isEmpty()) {
            groupByClause.append(", ");
        }

        append(fieldGetter, alias, groupByClause);

        return this;
    }

    public <T, R> SelectQueryBuilder groupBy(Class<T> tableClass) {
        return groupBy(tableClass, null);
    }

    public <T, R> SelectQueryBuilder groupBy(Class<T> tableClass,
            String alias) {
        TableMetadata table;

        table = getTable(tableClass);

        for (ColumnMetadata column : table.getColumns()) {
            if (!groupByClause.isEmpty()) {
                groupByClause.append(", ");
            }

            append(column, alias, groupByClause);
        }

        return this;
    }

    public SelectQueryBuilder groupBy(String expression) {
        return groupBy(expression, null);
    }

    public SelectQueryBuilder groupBy(String expression,
            List<Object> expressionParameters) {
        if (!groupByClause.isEmpty()) {
            groupByClause.append(", ");
        }

        groupByClause.append(expression);

        if (expressionParameters != null) {
            getParameters().addAll(expressionParameters);
        }

        return this;
    }

    public SelectQueryBuilder offset(int offset) {
        this.offset = offset;

        return this;
    }

    public SelectQueryBuilder limit(int limit) {
        this.limit = limit;

        return this;
    }

    public String build() {
        StringBuilder statement;

        statement = new StringBuilder();

        statement.append("SELECT ");
        statement.append(selectClause.toString());
        statement.append(" FROM ");
        statement.append(fromClause.toString());

        if (!getWhereClause().isEmpty()) {
            statement.append(getWhereClause().toString());
        }

        if (!groupByClause.isEmpty()) {
            statement.append(" GROUP BY ");
            statement.append(groupByClause.toString());
        }

        if (!orderByClause.isEmpty()) {
            statement.append(" ORDER BY ");
            statement.append(orderByClause.toString());
        }

        if (limit > 0) {
            statement.append(" ");
            statement.append(getTableManager().getDialect()
                    .limitSelect(offset, limit));
        }

        return statement.toString();
    }

    public <K, V> Map<K, List<V>> mapResultList(Class<K> keyType, Class<V> valueType) {
        return createQuery().mapResultList(keyType, valueType);
    }

    public <K, V> Map<K, V> mapSingleResult(Class<K> keyType, Class<V> valueType) {
        return createQuery().mapSingleResult(keyType, valueType);
    }

    public <T> List<T> getResultList(Class<T> type) {
        return createQuery().getResultList(type);
    }

    public List<Object[]> getResultList(Class<?>... tableClasses) {
        return createQuery().getResultList(tableClasses);
    }

    public <T> T getSingleResult(Class<T> type) {
        return createQuery().getSingleResult(type);
    }

    public Object[] getSingleResult(Class<?>... tableClasses) {
        return createQuery().getSingleResult(tableClasses);
    }

    private Query createQuery() {
        Query query;

        query = getTableManager().createQuery(build());

        for (int i = 0; i < getParameters().size(); i++) {
            query.setParameter(i + 1, getParameters().get(i));
        }

        return query;
    }

    private class FromClauseTable {
        private final TableMetadata table;
        private final String alias;

        public FromClauseTable(TableMetadata table, String alias) {
            this.table = table;

            this.alias = alias;
        }
    }
}
