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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.norm4j.TableManager;
import org.norm4j.schema.SchemaSynchronizer;
import org.norm4j.tests.BaseTest;

public class Test15 extends BaseTest {
    private TableManager tableManager;

    public Test15() {
    }

    @BeforeEach
    public void setup() {
        tableManager = new TableManager(getDataSource());

        new SchemaSynchronizer(tableManager)
                .version()
                .name("v0.1")
                .init(null)
                .finalize(null)
                .table(Author.class)
                .table(Book.class)
                .end()
                .apply()
                .version()
                .name("v0.2")
                .table(Order.class)
                .table(OrderItem.class)
                .end()
                .apply();

        new SchemaSynchronizer(tableManager)
                .version()
                .name("v0.3")
                .end()
                .apply();
    }

    @Test
    public void test15() {
    }

    @AfterEach
    void cleanup() {
        dropTable(null, "bookorderitem");
        dropTable(null, "bookorder");
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "schema_version");
    }
}
