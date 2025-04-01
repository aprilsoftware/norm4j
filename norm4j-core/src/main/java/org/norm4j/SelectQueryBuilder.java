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

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter)
    {
        return select(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder select(FieldGetter<T, R> fieldGetter, String alias)
    {
        ColumnMetadata column;

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        if (!selectClause.isEmpty())
        {
            selectClause.append(", ");
        }

        if (alias == null)
        {
            selectClause.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            selectClause.append(".");
            selectClause.append(column.getColumnName());
        }
        else
        {
            selectClause.append(alias);
            selectClause.append(".");
            selectClause.append(column.getColumnName());
        }

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

            if (alias == null)
            {
                selectClause.append(tableManager.getDialect()
                        .getTableName(table));
                selectClause.append(".");
                selectClause.append(column.getColumnName());
            }
            else
            {
                selectClause.append(alias);
                selectClause.append(".");
                selectClause.append(column.getColumnName());
            }
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

        appendWhereClause(fieldGetter, alias);

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

        appendWhereClause(fieldGetter, alias);

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

        appendWhereClause(leftFieldGetter, leftAlias);

        whereClause.append(" ");
        whereClause.append(operator);
        whereClause.append(" ");

        appendWhereClause(rightFieldGetter, rightAlias);

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

        appendWhereClause(fieldGetter, alias);

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

        appendWhereClause(fieldGetter, alias);

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
        if (whereClause.isEmpty())
        {
            whereClause.append(" WHERE ");
        }
        else
        {
            whereClause.append(" AND ");
        }

        whereClause.append(expression);

        return this;
    }

    private <T, R> void appendWhereClause(FieldGetter<T, R> fieldGetter, String alias)
    {
        ColumnMetadata column;

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        if (alias == null)
        {
            whereClause.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            whereClause.append(".");
            whereClause.append(column.getColumnName());
        }
        else
        {
            whereClause.append(alias);
            whereClause.append(".");
            whereClause.append(column.getColumnName());
        }
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter)
    {
        return orderBy(fieldGetter, null);
    }

    public <T, R> SelectQueryBuilder orderBy(FieldGetter<T, R> fieldGetter, String alias)
    {
        ColumnMetadata column;

        if (!orderByClause.isEmpty())
        {
            orderByClause.append(", ");
        }

        column = tableManager.getMetadataManager().getMetadata(fieldGetter);

        if (alias == null)
        {
            orderByClause.append(tableManager.getDialect()
                    .getTableName(column.getTable()));
            orderByClause.append(".");
            orderByClause.append(column.getColumnName());
        }
        else
        {
            orderByClause.append(alias);
            orderByClause.append(".");
            orderByClause.append(column.getColumnName());
        }

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

    public SelectQueryBuilder orderBy(String expression)
    {
        if (!orderByClause.isEmpty())
        {
            orderByClause.append(", ");
        }

        orderByClause.append(expression);

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

    public <T> List<T> getResultList(Class<T> tableClass)
    {
        Query query;

        query = tableManager.createQuery(build());

        for (int i = 0; i < parameters.size(); i++)
        {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getResultList(tableClass);
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
