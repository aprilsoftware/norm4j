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

import org.norm4j.metadata.TableMetadata;

public class DeleteQueryBuilder extends QueryBuilder<DeleteQueryBuilder>
{
    private final StringBuilder fromClause;

    public DeleteQueryBuilder(TableManager tableManager)
    {
        super(tableManager);

        fromClause = new StringBuilder();
    }

    protected DeleteQueryBuilder self()
    {
        return this;
    }

    public DeleteQueryBuilder from(Class<?> tableClass)
    {
        TableMetadata table;

        if (!fromClause.isEmpty())
        {
            throw new RuntimeException("from(...) must be called only once.");
        }

        table = getTable(tableClass);

        fromClause.append(getTableManager().getDialect()
                .getTableName(table));

        return this;
    }

    public String build()
    {
        StringBuilder statement;

        statement = new StringBuilder();

        statement.append("DELETE FROM ");
        statement.append(fromClause.toString());

        if (!getWhereClause().isEmpty())
        {
            statement.append(getWhereClause().toString());
        }

        return statement.toString();
    }

    public int executeUpdate()
    {
        Query query;

        query = getTableManager().createQuery(build());

        for (int i = 0; i < getParameters().size(); i++)
        {
            query.setParameter(i + 1, getParameters().get(i));
        }

        return query.executeUpdate();
    }
}
