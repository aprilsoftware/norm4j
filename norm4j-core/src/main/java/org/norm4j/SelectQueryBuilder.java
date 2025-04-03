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

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.TableMetadata;

public class SelectQueryBuilder
{
    private final List<FromClauseTable> fromClauseTables;
    private final TableManager tableManager;
    private final StringBuilder selectClause;
    private final StringBuilder fromClause;
    private final StringBuilder whereClause;
    private final StringBuilder orderByClause;
    private final StringBuilder groupByClause;
    private final List<Object> parameters;
    private int offset;
    private int limit;

    public SelectQueryBuilder(TableManager tableManager)
    {
        this.tableManager = tableManager;

        parameters = new ArrayList<>();

        fromClauseTables = new ArrayList<>();

        selectClause = new StringBuilder();

        fromClause = new StringBuilder();

        whereClause = new StringBuilder();

        orderByClause = new StringBuilder();

        groupByClause = new StringBuilder();
    }

    public SelectQueryBuilder count()
    {
        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        selectClause.append("count(*)");

        return this;
    }

    public <T, R> SelectQueryBuilder count(FieldGetter<T, R> fieldGetter)
    {
        return count(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder count(FieldGetter<T, R> fieldGetter, String alias)
    {
        appendAggregateFunction(fieldGetter, alias, "count");

        return this;
    }

    public <T, R> SelectQueryBuilder count(SelectQueryBuilder builder)
    {
        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        selectClause.append("count(");
        selectClause.append(builder.build());
        selectClause.append(")");

        return this;
    }

    public <T, R> SelectQueryBuilder sum(FieldGetter<T, R> fieldGetter)
    {
        return sum(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder sum(FieldGetter<T, R> fieldGetter, String alias)
    {
        appendAggregateFunction(fieldGetter, alias, "sum");

        return this;
    }

    public <T, R> SelectQueryBuilder avg(FieldGetter<T, R> fieldGetter)
    {
        return avg(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder avg(FieldGetter<T, R> fieldGetter, String alias)
    {
        appendAggregateFunction(fieldGetter, alias, "avg");

        return this;
    }

    public <T, R> SelectQueryBuilder min(FieldGetter<T, R> fieldGetter)
    {
        return min(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder min(FieldGetter<T, R> fieldGetter, String alias)
    {
        appendAggregateFunction(fieldGetter, alias, "min");

        return this;
    }

    public <T, R> SelectQueryBuilder max(FieldGetter<T, R> fieldGetter)
    {
        return max(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder max(FieldGetter<T, R> fieldGetter, String alias)
    {
        appendAggregateFunction(fieldGetter, alias, "max");

        return this;
    }

    private <T, R> void appendAggregateFunction(FieldGetter<T, R> fieldGetter, 
            String alias, 
            String aggregateFunction)
    {
        ColumnMetadata column;

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        selectClause.append(aggregateFunction);
        selectClause.append("(");

        append(column, alias, selectClause);

        selectClause.append(")");
    }

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter)
    {
        return select(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter, String alias)
    {
        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        append(fieldGetter, alias, selectClause);

        return this;
    }

    public SelectQueryBuilder select(Class<?> tableClass)
    {
        return select(tableClass, null);
    }

    public SelectQueryBuilder select(Class<?> tableClass, String alias)
    {
        TableMetadata table;

        table = getTable(tableClass);

        for (ColumnMetadata column : table.getColumns())
        {
            if (!selectClause.isEmpty())
            {
                selectClause.append(", ");
            }

            append(column, alias, selectClause);
        }

        return this;
    }

    public SelectQueryBuilder select(SelectQueryBuilder builder)
    {
        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        selectClause.append("(");
        selectClause.append(builder.build());
        selectClause.append(")");

        parameters.addAll(builder.parameters);

        return this;
    }

    public SelectQueryBuilder select(String expression)
    {
        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        selectClause.append(expression);

        return this;
    }

    public SelectQueryBuilder from(Class<?> tableClass)
    {
        return from(tableClass, null);
    }

    public SelectQueryBuilder from(Class<?> tableClass, String alias)
    {
        TableMetadata table;

        if (!fromClause.isEmpty())
        {
            throw new RuntimeException("from(...) must be called only once.");
        }

        table = getTable(tableClass);

        fromClause.append(tableManager.getDialect()
                .getTableName(table));

        if (alias != null)
        {
            fromClause.append(" AS ");
            fromClause.append(alias);
        }

        fromClauseTables.add(new FromClauseTable(table, alias));

        return this;
    }

    public SelectQueryBuilder from(SelectQueryBuilder builder, String alias)
    {
        if (!fromClause.isEmpty())
        {
            throw new RuntimeException("from(...) must be called only once.");
        }

        fromClause.append("(");
        fromClause.append(builder.build());
        fromClause.append(")");
        fromClause.append(" AS ");
        fromClause.append(alias);

        parameters.addAll(builder.parameters);

        return this;
    }

    public SelectQueryBuilder innerJoin(Class<?> tableClass)
    {
        return innerJoin(tableClass, null);
    }

    public SelectQueryBuilder innerJoin(Class<?> tableClass, String alias)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before innerJoin(...).");
        }

        join(tableClass, alias, "INNER JOIN");

        return this;
    }

    public SelectQueryBuilder innerJoin(SelectQueryBuilder builder, 
            String alias, 
            String joinExpression)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before innerJoin(...).");
        }

        join(builder, alias, joinExpression, "INNER JOIN");

        return this;
    }

    public SelectQueryBuilder leftJoin(Class<?> tableClass)
    {
        return leftJoin(tableClass, null);
    }

    public SelectQueryBuilder leftJoin(Class<?> tableClass, String alias)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before leftJoin(...).");
        }

        join(tableClass, alias, "LEFT JOIN");

        return this;
    }

    public SelectQueryBuilder leftJoin(SelectQueryBuilder builder, 
            String alias, 
            String joinExpression)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before leftJoin(...).");
        }

        join(builder, alias, joinExpression, "LEFT JOIN");

        return this;
    }

    public SelectQueryBuilder rightJoin(Class<?> tableClass)
    {
        return rightJoin(tableClass, null);
    }

    public SelectQueryBuilder rightJoin(Class<?> tableClass, String alias)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before rightJoin(...).");
        }

        join(tableClass, alias, "RIGHT JOIN");

        return this;
    }

    public SelectQueryBuilder rightJoin(SelectQueryBuilder builder, 
            String alias, 
            String joinExpression)
    {
        if (fromClause.isEmpty())
        {
            throw new RuntimeException("Call from(...) before rightJoin(...).");
        }

        join(builder, alias, joinExpression, "RIGHT JOIN");

        return this;
    }

    private void join(Class<?> tableClass, String alias, String joinType)
    {
        TableMetadata table;

        table = getTable(tableClass);

        fromClause.append(" ");
        fromClause.append(joinType);
        fromClause.append(" ");
        fromClause.append(tableManager.getDialect()
                .getTableName(table));

        if (alias != null)
        {
            fromClause.append(" AS ");
            fromClause.append(alias);
        }

        fromClause.append(" ON ");
        fromClause.append(getJoinExpression(table, alias));

        fromClauseTables.add(new FromClauseTable(table, alias));
    }

    private String getJoinExpression(TableMetadata table, String alias)
    {
        for (Join join : table.getJoins())
        {
            for (FromClauseTable fromClauseTable : fromClauseTables)
            {
                if (join.reference().table().equals(fromClauseTable.table.getTableClass()))
                {
                    StringBuilder expression;

                    expression = new StringBuilder();

                    for (int i = 0; i < join.columns().length; i++)
                    {
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

                        if (!expression.isEmpty())
                        {
                            expression.append(" AND ");
                        }

                        if (alias == null)
                        {
                            expression.append(tableManager.getDialect()
                                    .getTableName(table));
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }
                        else
                        {
                            expression.append(alias);
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }

                        expression.append(" = ");

                        if (fromClauseTable.alias == null)
                        {
                            expression.append(tableManager.getDialect()
                                    .getTableName(fromClauseTable.table));
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        }
                        else
                        {
                            expression.append(fromClauseTable.alias);
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        }
                    }

                    return expression.toString();
                }
            }
        }

        for (FromClauseTable fromClauseTable : fromClauseTables)
        {
            for (Join join : fromClauseTable.table.getJoins())
            {
                if (join.reference().table().equals(table.getTableClass()))
                {
                    StringBuilder expression;

                    expression = new StringBuilder();

                    for (int i = 0; i < join.columns().length; i++)
                    {
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

                        if (!expression.isEmpty())
                        {
                            expression.append(" AND ");
                        }

                        if (alias == null)
                        {
                            expression.append(tableManager.getDialect()
                                    .getTableName(table));
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }
                        else
                        {
                            expression.append(alias);
                            expression.append(".");
                            expression.append(leftColumn.getColumnName());
                        }

                        expression.append(" = ");

                        if (fromClauseTable.alias == null)
                        {
                            expression.append(tableManager.getDialect()
                                    .getTableName(fromClauseTable.table));
                            expression.append(".");
                            expression.append(rightColumn.getColumnName());
                        }
                        else
                        {
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
            String joinType)
    {
        fromClause.append(" ");
        fromClause.append(joinType);
        fromClause.append(" (");
        fromClause.append(builder.build());
        fromClause.append(") AS ");
        fromClause.append(alias);
        fromClause.append(" ON ");
        fromClause.append(joinExpression);

        parameters.addAll(builder.parameters);
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> fieldGetter, 
            String operator, 
            Object value)
    {
        return where(fieldGetter, null, operator, value);
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> fieldGetter, 
            String alias,
            String operator, 
            Object value)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        append(fieldGetter, alias, whereClause);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        whereClause.append("?");

        parameters.add(value);

        return this;
    }

    public <T, R> SelectQueryBuilder where(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return where(value, operator, fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder where(Object value, 
            String operator, 
            FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append("?");

        parameters.add(value);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        append(fieldGetter, alias, whereClause);

        return this;
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> leftFieldGetter, 
            String operator, 
            FieldGetter<T, R> rightFieldGetter)
    {
        return where(leftFieldGetter, 
                null, 
                operator, 
                rightFieldGetter, 
                null);
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> leftFieldGetter, 
            String leftAlias,
            String operator, 
            FieldGetter<T, R> rightFieldGetter,
            String rightAlias)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        append(leftFieldGetter, leftAlias, whereClause);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        append(rightFieldGetter, rightAlias, whereClause);

        return this;
    }

    public SelectQueryBuilder where(SelectQueryBuilder leftBuilder,
            String operator, 
            SelectQueryBuilder rightBuilder)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append("(");
        whereClause.append(leftBuilder.build());
        whereClause.append(")");

        parameters.addAll(leftBuilder.parameters);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        whereClause.append("(");
        whereClause.append(rightBuilder.build());
        whereClause.append(")");

        parameters.addAll(rightBuilder.parameters);

        return this;
    }

    public SelectQueryBuilder where(SelectQueryBuilder builder,
            String operator, 
            Object value)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append("(");
        whereClause.append(builder.build());
        whereClause.append(")");

        parameters.addAll(builder.parameters);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        whereClause.append("?");

        parameters.add(value);

        return this;
    }

    public SelectQueryBuilder where(Object value,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append("?");

        parameters.add(value);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        whereClause.append("(");
        whereClause.append(builder.build());
        whereClause.append(")");

        parameters.addAll(builder.parameters);

        return this;
    }

    public <T, R> SelectQueryBuilder where(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter)
    {
        return where(builder, operator, fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder where(SelectQueryBuilder builder,
            String operator, 
            FieldGetter<T, R> fieldGetter,
            String alias)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append("(");
        whereClause.append(builder.build());
        whereClause.append(")");

        parameters.addAll(builder.parameters);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        append(fieldGetter, alias, whereClause);

        return this;
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> fieldGetter,
            String operator, 
            SelectQueryBuilder builder)
    {
        return where(fieldGetter, null, operator, builder);
    }

    public <T, R> SelectQueryBuilder where(FieldGetter<T, R> fieldGetter,
            String alias,
            String operator, 
            SelectQueryBuilder builder)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        append(fieldGetter, alias, whereClause);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        whereClause.append("(");
        whereClause.append(builder.build());
        whereClause.append(")");

        parameters.addAll(builder.parameters);

        return this;
    }

    public SelectQueryBuilder where(String expression)
    {
        return where(expression, null);
    }

    public SelectQueryBuilder where(String expression, List<Object> parameters)
    {
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append(expression);

        if (parameters != null)
        {
            this.parameters.addAll(parameters);
        }

        return this;
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter)
    {
        return orderBy(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter, String alias)
    {
        if (!orderByClause.isEmpty())
        {
            orderByClause.append(", ");
        }

        append(fieldGetter, alias, orderByClause);

        return this;
    }

    public <T, R> SelectQueryBuilder orderByDesc(FieldGetter<T, R> fieldGetter)
    {
        return orderByDesc(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder orderByDesc(FieldGetter<T, R> fieldGetter, String alias)
    {
        orderBy(fieldGetter, alias);

        orderByClause.append(" DESC");

        return this;
    }

    public SelectQueryBuilder orderBy(SelectQueryBuilder builder)
    {
        if (!orderByClause.isEmpty())
        {
            orderByClause.append(", ");
        }

        orderByClause.append("(");
        orderByClause.append(builder.build());
        orderByClause.append(")");

        parameters.addAll(builder.parameters);

        return this;
    }

    public <T, R> SelectQueryBuilder orderByDesc(SelectQueryBuilder builder)
    {
        orderBy(builder);

        orderByClause.append(" DESC");

        return this;
    }

    public SelectQueryBuilder orderBy(String expression)
    {
        if (!orderByClause.isEmpty())
        {
            orderByClause.append(", ");
        }

        orderByClause.append(expression);

        return this;
    }

    public <T, R> SelectQueryBuilder groupBy(FieldGetter<T, R> fieldGetter)
    {
        return groupBy(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder groupBy(FieldGetter<T, R> fieldGetter, 
            String alias)
    {
        if (!groupByClause.isEmpty())
        {
            groupByClause.append(", ");
        }

        append(fieldGetter, alias, groupByClause);

        return this;
    }

    public SelectQueryBuilder groupBy(String expression)
    {
        if (!groupByClause.isEmpty())
        {
            groupByClause.append(", ");
        }

        groupByClause.append(expression);

        return this;
    }

    public SelectQueryBuilder offset(int offset)
    {
        this.offset = offset;

        return this;
    }

    public SelectQueryBuilder limit(int limit)
    {
        this.limit = limit;

        return this;
    }

    public String build()
    {
        StringBuilder statement;

        statement = new StringBuilder();

        statement.append("SELECT ");
        statement.append(selectClause.toString());
        statement.append(" FROM ");
        statement.append(fromClause.toString());

        if (!whereClause.isEmpty())
        {
            statement.append(whereClause.toString());
        }

        if (!groupByClause.isEmpty())
        {
            statement.append(" GROUP BY ");
            statement.append(groupByClause.toString());
        }

        if (!orderByClause.isEmpty())
        {
            statement.append(" ORDER BY ");
            statement.append(orderByClause.toString());
        }

        if (limit > 0)
        {
            statement.append(" ");
            statement.append(tableManager.getDialect().limitSelect(offset, limit));
        }

        return statement.toString();
    }

    public <T> List<T> getResultList(Class<T> type)
    {
        Query query;

        query = tableManager.createQuery(build());

        for (int i = 0; i < parameters.size(); i++)
        {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getResultList(type);
    }

    public List<Object[]> getResultList(Class<?>... tableClasses)
    {
        Query query;

        query = tableManager.createQuery(build());

        for (int i = 0; i < parameters.size(); i++)
        {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getResultList(tableClasses);
    }

    public <T> T getSingleResult(Class<T> type)
    {
        Query query;

        query = tableManager.createQuery(build());

        for (int i = 0; i < parameters.size(); i++)
        {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getSingleResult(type);
    }

    public Object[] getSingleResult(Class<?>... tableClasses)
    {
        Query query;

        query = tableManager.createQuery(build());

        for (int i = 0; i < parameters.size(); i++)
        {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getSingleResult(tableClasses);
    }

    private <T, R> void append(FieldGetter<T, R> fieldGetter, 
            String alias, 
            StringBuilder sb)
    {
        append(tableManager.getMetadataManager().getMetadata(fieldGetter), 
                alias, 
                sb);
    }

    private <T, R> void append(ColumnMetadata column, 
            String alias, 
            StringBuilder sb)
    {
        if (alias == null)
        {
            sb.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            sb.append(".");
            sb.append(column.getColumnName());
        }
        else
        {
            sb.append(alias);
            sb.append(".");
            sb.append(column.getColumnName());
        }
    }

    private TableMetadata getTable(Class<?> tableClass)
    {
        TableMetadata table;

        table = tableManager.getMetadataManager().getMetadata(tableClass);

        if (table == null)
        {
            throw new IllegalArgumentException("No metadata found for class " 
                    + tableClass.getName());
        }
        else
        {
            return table;
        }
    }

    private class FromClauseTable
    {
        private final TableMetadata table;
        private final String alias;

        public FromClauseTable(TableMetadata table, String alias)
        {
            this.table = table;

            this.alias = alias;
        }
    }
}
