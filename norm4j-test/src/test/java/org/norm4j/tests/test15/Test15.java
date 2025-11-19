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
package org.norm4j.tests.test15;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.norm4j.TableManager;
import org.norm4j.dialects.MariaDBDialect;
import org.norm4j.dialects.OracleDialect;
import org.norm4j.dialects.PostgreSQLDialect;
import org.norm4j.dialects.SQLServerDialect;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.schema.SchemaSynchronizer;
import org.norm4j.tests.BaseTest;

public class Test15 extends BaseTest {
    private TableManager tableManager;

    public Test15() {
    }

    @BeforeEach
    public void setup() {
        MetadataManager metadataManager;

        metadataManager = new MetadataManager();

        metadataManager.registerPackage("org.norm4j.tests.test15");

        tableManager = new TableManager(getDataSource(), metadataManager);

        new SchemaSynchronizer(tableManager)
                .version()
                .name("v0.1")
                .executeResourceIfInitial("db/v0.1/mariadb/ddl.sql", MariaDBDialect.class)
                .executeResourceIfInitial("db/v0.1/oracle/ddl.sql", OracleDialect.class)
                .executeResourceIfInitial("db/v0.1/postgresql/ddl.sql", PostgreSQLDialect.class)
                .executeResourceIfInitial("db/v0.1/sqlserver/ddl.sql", SQLServerDialect.class)
                .endVersion()
                .version()
                .name("v0.2")

                .executeResourceIfInitial("db/v0.2/mariadb/ddl.sql", MariaDBDialect.class)
                .executeResourceIfInitial("db/v0.2/oracle/ddl.sql", OracleDialect.class)
                .executeResourceIfInitial("db/v0.2/postgresql/ddl.sql", PostgreSQLDialect.class)
                .executeResourceIfInitial("db/v0.2/sqlserver/ddl.sql", SQLServerDialect.class)
                .executeIfInitial("insert into bookorder (orderdate) values ('2025-11-17');")
                .executeIfInitial(tableManager.createQuery("delete from bookorder;"))
                .executeResourceIfInitial("db/test15/v0.2/test.sql")

                .execute("insert into bookorder (orderdate) values ('2025-11-17');")
                .execute(tableManager.createQuery("delete from bookorder;"))
                .executeResource("db/test15/v0.2/test.sql")

                .endVersion()
                .apply();
    }

    @Test
    public void test15() {
        List<Order> orders = tableManager.createSelectQueryBuilder()
                .select()
                .from(Order.class)
                .getResultList(Order.class);

        assertEquals(1, orders.size());
    }

    @AfterEach
    void cleanup() {
        dropTable("bookorderitem");
        dropTable("bookorder");
        dropTable("book");
        dropTable("author");
        dropTable("schema_version");
    }
}
