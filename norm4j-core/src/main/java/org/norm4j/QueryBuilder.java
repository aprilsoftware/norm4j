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
import java.util.function.Consumer;

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public abstract class QueryBuilder<Q extends QueryBuilder<Q>> {
    private final TableManager tableManager;
    private final List<Object> parameters;
    private final StringBuilder whereClause;

    public QueryBuilder(TableManager tableManager) {
        this.tableManager = tableManager;

        parameters = new ArrayList<>();

        whereClause = new StringBuilder();
    }

    public abstract String build();

    protected TableManager getTableManager() {
        return tableManager;
    }

    protected List<Object> getParameters() {
        return parameters;
    }

    protected abstract Q self();

    protected StringBuilder getWhereClause() {
        return whereClause;
    }

    public Q where(Object leftValue,
            String operator,
            Object rightValue) {
        appendWhere();

        appendCondition(leftValue,
                operator,
                rightValue,
                whereClause);

        return self();
    }

    public Q where(ColumnMetadata column,
            String operator,
            Object value) {
        return where(column, null, operator, value);
    }

    public Q where(ColumnMetadata column,
            String alias,
            String operator,
            Object value) {
        appendWhere();

        appendCondition(column,
                alias,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(List<ColumnMetadata> columns,
            String operator,
            List<List<Object>> values) {
        return where(columns, null, operator, values);
    }

    public Q where(List<ColumnMetadata> columns,
            String alias,
            String operator,
            List<List<Object>> values) {
        appendWhere();

        appendCondition(columns,
                alias,
                operator,
                values,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q where(FieldGetter<T, R> fieldGetter,
            String operator,
            Object value) {
        return where(fieldGetter, null, operator, value);
    }

    public <T, R> Q where(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            Object value) {
        appendWhere();

        appendCondition(fieldGetter,
                alias,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q where(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return where(value, operator, fieldGetter, null);
    }

    public <T, R> Q where(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        appendWhere();

        appendCondition(value,
                operator,
                fieldGetter,
                alias,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R, S, U> Q where(FieldGetter<T, R> leftFieldGetter,
            String operator,
            FieldGetter<S, U> rightFieldGetter) {
        return where(leftFieldGetter,
                null,
                operator,
                rightFieldGetter,
                null);
    }

    public <T, R, S, U> Q where(FieldGetter<T, R> leftFieldGetter,
            String leftAlias,
            String operator,
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias) {
        appendWhere();

        appendCondition(leftFieldGetter,
                leftAlias,
                operator,
                rightFieldGetter,
                rightAlias,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(SelectQueryBuilder leftBuilder,
            String operator,
            SelectQueryBuilder rightBuilder) {
        appendWhere();

        appendCondition(leftBuilder,
                operator,
                rightBuilder,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(SelectQueryBuilder builder,
            String operator,
            Object value) {
        appendWhere();

        appendCondition(builder,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(Object value,
            String operator,
            SelectQueryBuilder builder) {
        appendWhere();

        appendCondition(value,
                operator,
                builder,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q where(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return where(builder, operator, fieldGetter, null);
    }

    public <T, R> Q where(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        appendWhere();

        appendCondition(builder,
                operator,
                fieldGetter,
                alias,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q where(FieldGetter<T, R> fieldGetter,
            String operator,
            SelectQueryBuilder builder) {
        return where(fieldGetter, null, operator, builder);
    }

    public <T, R> Q where(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            SelectQueryBuilder builder) {
        appendWhere();

        appendCondition(fieldGetter,
                alias,
                operator,
                builder,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(Expression expression,
            String operator,
            Object value) {
        appendWhere();

        appendCondition(expression,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(String expression) {
        return where(expression, null);
    }

    public Q where(String expression, List<Object> parameters) {
        appendWhere();

        appendCondition(expression,
                parameters,
                whereClause,
                getParameters());

        return self();
    }

    public Q where(Consumer<ConditionBuilder> consumer) {
        appendWhere();

        appendCondition(consumer,
                whereClause,
                getParameters());

        return self();
    }

    public Q and(Object leftValue,
            String operator,
            Object rightValue) {
        return where(leftValue, operator, rightValue);
    }

    public Q and(ColumnMetadata column,
            String operator,
            Object value) {
        return where(column, operator, value);
    }

    public Q and(ColumnMetadata column,
            String alias,
            String operator,
            Object value) {
        return where(column, alias, operator, value);
    }

    public Q and(List<ColumnMetadata> columns,
            String operator,
            List<List<Object>> values) {
        return where(columns, operator, values);
    }

    public Q and(List<ColumnMetadata> columns,
            String alias,
            String operator,
            List<List<Object>> values) {
        return where(columns, alias, operator, values);
    }

    public <T, R> Q and(FieldGetter<T, R> fieldGetter,
            String operator,
            Object value) {
        return where(fieldGetter, operator, value);
    }

    public <T, R> Q and(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            Object value) {
        return where(fieldGetter, alias, operator, value);
    }

    public <T, R> Q and(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return where(value, operator, fieldGetter);
    }

    public <T, R> Q and(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        return where(value, operator, fieldGetter, alias);
    }

    public <T, R, S, U> Q and(FieldGetter<T, R> leftFieldGetter,
            String operator,
            FieldGetter<S, U> rightFieldGetter) {
        return where(leftFieldGetter,
                operator,
                rightFieldGetter);
    }

    public <T, R, S, U> Q and(FieldGetter<T, R> leftFieldGetter,
            String leftAlias,
            String operator,
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias) {
        return where(leftFieldGetter,
                leftAlias,
                operator,
                rightFieldGetter,
                rightAlias);
    }

    public Q and(SelectQueryBuilder leftBuilder,
            String operator,
            SelectQueryBuilder rightBuilder) {
        return where(leftBuilder, operator, rightBuilder);
    }

    public Q and(SelectQueryBuilder builder,
            String operator,
            Object value) {
        return where(builder, operator, value);
    }

    public Q and(Object value,
            String operator,
            SelectQueryBuilder builder) {
        return where(value, operator, builder);
    }

    public <T, R> Q and(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return where(builder, operator, fieldGetter);
    }

    public <T, R> Q and(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        return where(builder, operator, fieldGetter, alias);
    }

    public <T, R> Q and(FieldGetter<T, R> fieldGetter,
            String operator,
            SelectQueryBuilder builder) {
        return where(fieldGetter, operator, builder);
    }

    public <T, R> Q and(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            SelectQueryBuilder builder) {
        return where(fieldGetter, alias, operator, builder);
    }

    public Q and(Expression expression,
            String operator,
            Object value) {
        return where(expression, operator, value);
    }

    public Q and(String expression) {
        return where(expression);
    }

    public Q and(String expression, List<Object> parameters) {
        return where(expression, parameters);
    }

    public Q and(Consumer<ConditionBuilder> consumer) {
        return where(consumer);
    }

    public Q or(Object leftValue,
            String operator,
            Object rightValue) {
        appendOr();

        appendCondition(leftValue,
                operator,
                rightValue,
                whereClause);

        return self();
    }

    public Q or(ColumnMetadata column,
            String operator,
            Object value) {
        return or(column, null, operator, value);
    }

    public Q or(ColumnMetadata column,
            String alias,
            String operator,
            Object value) {
        appendOr();

        appendCondition(column,
                alias,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(List<ColumnMetadata> columns,
            String operator,
            List<List<Object>> values) {
        return or(columns, null, operator, values);
    }

    public Q or(List<ColumnMetadata> columns,
            String alias,
            String operator,
            List<List<Object>> values) {
        appendOr();

        appendCondition(columns,
                alias,
                operator,
                values,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q or(FieldGetter<T, R> fieldGetter,
            String operator,
            Object value) {
        return or(fieldGetter, null, operator, value);
    }

    public <T, R> Q or(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            Object value) {
        appendOr();

        appendCondition(fieldGetter,
                alias,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q or(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return or(value, operator, fieldGetter, null);
    }

    public <T, R> Q or(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        appendOr();

        appendCondition(value,
                operator,
                fieldGetter,
                alias,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R, S, U> Q or(FieldGetter<T, R> leftFieldGetter,
            String operator,
            FieldGetter<S, U> rightFieldGetter) {
        return or(leftFieldGetter,
                null,
                operator,
                rightFieldGetter,
                null);
    }

    public <T, R, S, U> Q or(FieldGetter<T, R> leftFieldGetter,
            String leftAlias,
            String operator,
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias) {
        appendOr();

        appendCondition(leftFieldGetter,
                leftAlias,
                operator,
                rightFieldGetter,
                rightAlias,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(SelectQueryBuilder leftBuilder,
            String operator,
            SelectQueryBuilder rightBuilder) {
        appendOr();

        appendCondition(leftBuilder,
                operator,
                rightBuilder,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(SelectQueryBuilder builder,
            String operator,
            Object value) {
        appendOr();

        appendCondition(builder,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(Object value,
            String operator,
            SelectQueryBuilder builder) {
        appendOr();

        appendCondition(value,
                operator,
                builder,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q or(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter) {
        return or(builder, operator, fieldGetter, null);
    }

    public <T, R> Q or(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias) {
        appendOr();

        appendCondition(builder,
                operator,
                fieldGetter,
                alias,
                whereClause,
                getParameters());

        return self();
    }

    public <T, R> Q or(FieldGetter<T, R> fieldGetter,
            String operator,
            SelectQueryBuilder builder) {
        return or(fieldGetter, null, operator, builder);
    }

    public <T, R> Q or(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            SelectQueryBuilder builder) {
        appendOr();

        appendCondition(fieldGetter,
                alias,
                operator,
                builder,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(Expression expression,
            String operator,
            Object value) {
        appendOr();

        appendCondition(expression,
                operator,
                value,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(String expression) {
        return or(expression, null);
    }

    public Q or(String expression, List<Object> parameters) {
        appendOr();

        appendCondition(expression,
                parameters,
                whereClause,
                getParameters());

        return self();
    }

    public Q or(Consumer<ConditionBuilder> consumer) {
        appendOr();

        appendCondition(consumer,
                whereClause,
                getParameters());

        return self();
    }

    protected void appendCondition(Object leftValue,
            String operator,
            Object rightValue,
            StringBuilder condition) {
        appendValue(leftValue, condition, null);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        appendValue(rightValue, condition, null);
    }

    protected <T, R> void appendCondition(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            Object value,
            StringBuilder condition,
            List<Object> parameters) {
        ColumnMetadata column;

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        append(column, alias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        appendValue(value, condition, column);
    }

    protected <T, R> void appendCondition(ColumnMetadata column,
            String alias,
            String operator,
            Object value,
            StringBuilder condition,
            List<Object> parameters) {
        append(column, alias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        appendValue(value, condition, column);
    }

    protected <T, R> void appendCondition(List<ColumnMetadata> columns,
            String alias,
            String operator,
            List<List<Object>> values,
            StringBuilder condition,
            List<Object> parameters) {
        int index;

        if (columns.size() < 2) {
            throw new RuntimeException("Invalid tuple.");
        }

        condition.append("(");

        index = 0;

        for (ColumnMetadata column : columns) {
            if (index > 0) {
                condition.append(", ");
            }

            append(column, alias, condition);

            index++;
        }

        condition.append(")");

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");

        index = 0;

        for (List<Object> tupleValues : values) {
            if (columns.size() != tupleValues.size()) {
                throw new RuntimeException("Invalid tuple.");
            }

            if (index > 0) {
                condition.append(", ");
            }

            condition.append("(");

            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    condition.append(", ");
                }

                appendValue(tupleValues.get(i), condition, columns.get(i));
            }

            condition.append(")");

            index++;
        }

        condition.append(")");
    }

    protected <T, R> void appendCondition(Object value,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias,
            StringBuilder condition,
            List<Object> parameters) {
        ColumnMetadata column;

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        appendValue(value, condition, column);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(column, alias, condition);
    }

    protected <T, R, S, U> void appendCondition(FieldGetter<T, R> leftFieldGetter,
            String leftAlias,
            String operator,
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias,
            StringBuilder condition,
            List<Object> parameters) {
        append(leftFieldGetter, leftAlias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(rightFieldGetter, rightAlias, condition);
    }

    protected void appendCondition(SelectQueryBuilder leftBuilder,
            String operator,
            SelectQueryBuilder rightBuilder,
            StringBuilder condition,
            List<Object> parameters) {
        condition.append("(");
        condition.append(leftBuilder.build());
        condition.append(")");

        getParameters().addAll(leftBuilder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(rightBuilder.build());
        condition.append(")");

        getParameters().addAll(rightBuilder.getParameters());
    }

    protected void appendCondition(SelectQueryBuilder builder,
            String operator,
            Object value,
            StringBuilder condition,
            List<Object> parameters) {
        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        appendValue(value, condition, null);
    }

    protected void appendCondition(Object value,
            String operator,
            SelectQueryBuilder builder,
            StringBuilder condition,
            List<Object> parameters) {
        appendValue(value, condition, null);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());
    }

    protected <T, R> void appendCondition(SelectQueryBuilder builder,
            String operator,
            FieldGetter<T, R> fieldGetter,
            String alias,
            StringBuilder condition,
            List<Object> parameters) {
        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        append(fieldGetter, alias, condition);
    }

    protected <T, R> void appendCondition(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator,
            SelectQueryBuilder builder,
            StringBuilder condition,
            List<Object> parameters) {
        append(fieldGetter, alias, condition);

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        getParameters().addAll(builder.getParameters());
    }

    protected <T, R> void appendCondition(Expression expression,
            String operator,
            Object value,
            StringBuilder condition,
            List<Object> parameters) {
        condition.append(expression.build(this));

        condition.append(" ");
        condition.append(operator);
        condition.append(" ");

        appendValue(value, condition, null);
    }

    protected void appendCondition(String expression,
            List<Object> expressionParameters,
            StringBuilder condition,
            List<Object> parameters) {
        condition.append(expression);

        if (expressionParameters != null) {
            parameters.addAll(expressionParameters);
        }
    }

    protected void appendCondition(Consumer<ConditionBuilder> consumer,
            StringBuilder condition,
            List<Object> parameters) {
        ConditionBuilder builder;

        builder = new ConditionBuilder(this);

        consumer.accept(builder);

        condition.append("(");
        condition.append(builder.build());
        condition.append(")");

        parameters.addAll(builder.getParameters());
    }

    protected <T, R> void append(FieldGetter<T, R> fieldGetter,
            String alias,
            StringBuilder sb) {
        append(tableManager.getMetadataManager().getMetadata(fieldGetter),
                alias,
                sb);
    }

    protected <T, R> void append(ColumnMetadata column,
            String alias,
            StringBuilder sb) {
        if (alias == null) {
            sb.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            sb.append(".");
            sb.append(column.getColumnName());
        } else {
            sb.append(alias);
            sb.append(".");
            sb.append(column.getColumnName());
        }
    }

    protected TableMetadata getTable(Class<?> tableClass) {
        TableMetadata table;

        table = tableManager.getMetadataManager().getMetadata(tableClass);

        if (table == null) {
            throw new IllegalArgumentException("No metadata found for class "
                    + tableClass.getName());
        } else {
            return table;
        }
    }

    @SuppressWarnings("rawtypes")
    protected void appendValue(Object value,
            StringBuilder clause,
            ColumnMetadata column) {
        if (value == null) {
            clause.append("NULL");
        } else {
            if (value instanceof Iterable) {
                int i = 0;

                clause.append("(");

                for (Object element : (Iterable) value) {
                    if (i > 0) {
                        clause.append(", ");
                    }

                    appendValue(element, clause, column);

                    i++;
                }

                clause.append(")");
            } else if (value.getClass().isArray()) {
                Object[] elements;

                elements = (Object[]) value;

                clause.append("(");

                for (int i = 0; i < elements.length; i++) {
                    if (i > 0) {
                        clause.append(", ");
                    }

                    appendValue(elements[i], clause, column);
                }

                clause.append(")");
            } else {
                clause.append("?");

                if (column == null) {
                    if (value.getClass().isEnum()) {
                        value = ((Enum<?>) value).ordinal();
                    } else if (value instanceof java.util.Date) {
                        value = new java.sql.Date(((java.util.Date) value).getTime());
                    }
                } else {
                    value = tableManager.getDialect().toSqlValue(column, value);
                }

                getParameters().add(value);
            }
        }
    }

    private void appendWhere() {
        if (whereClause.isEmpty()) {
            whereClause.append(" WHERE ");
        } else {
            whereClause.append(" AND ");
        }
    }

    private void appendOr() {
        if (whereClause.isEmpty()) {
            whereClause.append(" WHERE ");
        } else {
            whereClause.append(" OR ");
        }
    }
}
