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

public class ConditionBuilder
{
    private final QueryBuilder queryBuilder;
    private final StringBuilder condition;
    private final List<Object> parameters;

    public ConditionBuilder(QueryBuilder queryBuilder)
    {
        this.queryBuilder = queryBuilder;

        this.condition = new StringBuilder();

        this.parameters = new ArrayList<>();
    }

    protected List<Object> getParameters()
    {
        return parameters;
    }

    public ConditionBuilder condition(Object leftValue, 
            String operator, 
            Object rightValue)
    {
        appendCondition();

        queryBuilder.appendCondition(leftValue, 
                operator, 
                rightValue, 
                condition);

        return this;
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return condition(fieldGetter, null, operator, value);
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        appendCondition();
        
        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(value, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder condition(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        appendCondition();

        queryBuilder.appendCondition(value, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R, S, U> ConditionBuilder condition(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<S, U> rightFieldGetter)
    {
        return condition(leftFieldGetter, 
                null, 
                operator, 
                rightFieldGetter, 
                null);
    }

    public <T, R, S, U> ConditionBuilder condition(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias)
    {
        appendCondition();

        queryBuilder.appendCondition(leftFieldGetter, 
                leftAlias, 
                operator, 
                rightFieldGetter, 
                rightAlias, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        appendCondition();

        queryBuilder.appendCondition(leftBuilder, 
                operator, 
                rightBuilder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        appendCondition();

        queryBuilder.appendCondition(builder, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        appendCondition();

        queryBuilder.appendCondition(value, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(builder, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder condition(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        appendCondition();

        queryBuilder.appendCondition(builder, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, null, operator, builder);
    }

    public <T, R> ConditionBuilder condition(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        appendCondition();

        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Expression expression,
            String operator, 
            Object value)
    {
        appendCondition();

        queryBuilder.appendCondition(expression,
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(String expression)
    {
        return condition(expression, null);
    }

    public ConditionBuilder condition(String expression, List<Object> parameters)
    {
        appendCondition();

        queryBuilder.appendCondition(expression, 
                parameters, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder condition(Consumer<ConditionBuilder> consumer)
    {
        appendCondition();

        queryBuilder.appendCondition(consumer, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder and(Object leftValue, 
            String operator, 
            Object rightValue)
    {
        return condition(leftValue, operator, rightValue);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return condition(fieldGetter, operator, value);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        return condition(fieldGetter, alias, operator, value);
    }

    public <T, R> ConditionBuilder and(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(value, operator, fieldGetter);
    }

    public <T, R> ConditionBuilder and(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        return condition(value, operator, fieldGetter, alias);
    }

    public <T, R, S, U> ConditionBuilder and(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<S, U> rightFieldGetter)
    {
        return condition(leftFieldGetter, 
                operator, 
                rightFieldGetter);
    }

    public <T, R, S, U> ConditionBuilder and(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias)
    {
        return condition(leftFieldGetter, 
                leftAlias, 
                operator, 
                rightFieldGetter, 
                rightAlias);
    }

    public ConditionBuilder and(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        return condition(leftBuilder, operator, rightBuilder);
    }

    public ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        return condition(builder, operator, value);
    }

    public ConditionBuilder and(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(value, operator, builder);
    }

    public <T, R> ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return condition(builder, operator, fieldGetter);
    }

    public <T, R> ConditionBuilder and(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        return condition(builder, operator, fieldGetter, alias);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, operator, builder);
    }

    public <T, R> ConditionBuilder and(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        return condition(fieldGetter, alias, operator, builder);
    }

    public ConditionBuilder and(Expression expression,
            String operator, 
            Object value)
    {
        return condition(expression, operator, value);
    }

    public ConditionBuilder and(String expression)
    {
        return condition(expression);
    }

    public ConditionBuilder and(String expression, List<Object> parameters)
    {
        return condition(expression, parameters);
    }

    public ConditionBuilder and(Consumer<ConditionBuilder> consumer)
    {
        return condition(consumer);
    }

    public ConditionBuilder or(Object leftValue, 
            String operator, 
            Object rightValue)
    {
        appendOr();

        queryBuilder.appendCondition(leftValue, 
                operator, 
                rightValue, 
                condition);

        return this;
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return or(fieldGetter, null, operator, value);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        appendOr();
        
        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return or(value, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder or(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        appendOr();

        queryBuilder.appendCondition(value, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R, S, U> ConditionBuilder or(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<S, U> rightFieldGetter)
    {
        return or(leftFieldGetter, 
                null, 
                operator, 
                rightFieldGetter, 
                null);
    }

    public <T, R, S, U> ConditionBuilder or(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<S, U> rightFieldGetter,
            String rightAlias)
    {
        appendOr();

        queryBuilder.appendCondition(leftFieldGetter, 
                leftAlias, 
                operator, 
                rightFieldGetter, 
                rightAlias, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        appendOr();

        queryBuilder.appendCondition(leftBuilder, 
                operator, 
                rightBuilder, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        appendOr();

        queryBuilder.appendCondition(builder, 
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        appendOr();

        queryBuilder.appendCondition(value, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return or(builder, operator, fieldGetter, null);
    }

    public <T, R> ConditionBuilder or(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        appendOr();

        queryBuilder.appendCondition(builder, 
                operator, 
                fieldGetter, 
                alias, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return or(fieldGetter, null, operator, builder);
    }

    public <T, R> ConditionBuilder or(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        appendOr();

        queryBuilder.appendCondition(fieldGetter, 
                alias, 
                operator, 
                builder, 
                condition, 
                getParameters());

        return this;
    }

    public <T, R> ConditionBuilder or(Expression expression,
            String operator, 
            Object value)
    {
        appendOr();

        queryBuilder.appendCondition(expression,
                operator, 
                value, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(String expression)
    {
        return or(expression, null);
    }

    public ConditionBuilder or(String expression, List<Object> parameters)
    {
        appendOr();

        queryBuilder.appendCondition(expression, 
                parameters, 
                condition, 
                getParameters());

        return this;
    }

    public ConditionBuilder or(Consumer<ConditionBuilder> consumer)
    {
        appendOr();

        queryBuilder.appendCondition(consumer, 
                condition, 
                getParameters());

        return this;
    }

    public String build()
    {
        return condition.toString();
    }

    private void appendCondition()
    {
        if (!condition.isEmpty())
        {
            condition.append(" AND ");
        }
    }

    private void appendOr()
    {
        if (!condition.isEmpty())
        {
            condition.append(" OR ");
        }
    }
}
