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
package org.norm4j.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.norm4j.dialects.SQLDialect;

public abstract class BaseTest
{
    private static BasicDataSource dataSource;

    public BaseTest()
    {
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    @BeforeAll
    public static void initialize()
    {
        Properties properties;

        properties = new Properties();

        try (FileInputStream fis = new FileInputStream("src/test/resources/application-test.properties"))
        {
            properties.load(fis);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load database properties", e);
        }

        dataSource = new BasicDataSource();

        dataSource.setDriverClassName(properties.getProperty("datasource.driver"));
        dataSource.setUrl(properties.getProperty("datasource.url"));
        dataSource.setUsername(properties.getProperty("datasource.username"));
        dataSource.setPassword(properties.getProperty("datasource.password"));

        dataSource.setInitialSize(1);
        dataSource.setMaxTotal(2);
        dataSource.setMaxIdle(1);
        dataSource.setMinIdle(1);
    }

    protected boolean isArraySupported()
    {
        try (Connection connection = dataSource.getConnection())
        {
            return SQLDialect.detectDialect(connection).isArraySupported();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected boolean isSequenceSupported()
    {
        try (Connection connection = dataSource.getConnection())
        {
            return SQLDialect.detectDialect(connection).isSequenceSupported();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void dropSequence(String schema, String sequenceName)
    {
        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;

            dialect = SQLDialect.detectDialect(connection);

            if (dialect.sequenceExists(connection, schema, sequenceName))
            {
                if (schema == null || schema.isEmpty())
                {
                    executeUpdate(connection, "DROP SEQUENCE "
                            + sequenceName
                            + ";");
                }
                else
                {
                    executeUpdate(connection, "DROP SEQUENCE "
                            + schema
                            + "."
                            + sequenceName
                            + ";");
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void dropTable(String schema, String tableName)
    {
        try (Connection connection = dataSource.getConnection())
        {
            SQLDialect dialect;

            dialect = SQLDialect.detectDialect(connection);

            if (dialect.tableExists(connection, schema, tableName))
            {
                executeUpdate(connection, "DROP TABLE "
                        + dialect.getTableName(schema, tableName)
                        + ";");
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void executeUpdate(Connection connection, String sql)
    {
        try (Statement stmt = connection.createStatement())
        {
            stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
