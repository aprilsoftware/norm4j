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
package org.norm4j.tests.test11;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.norm4j.Functions;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test11 extends BaseTest {
    private TableManager tableManager;

    public Test11() {
    }

    @BeforeEach
    public void setup() {
        MetadataManager metadataManager;
        Tenant tenant;
        Author author;
        Book book1;
        Book book2;

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");

        metadataManager = new MetadataManager();

        metadataManager.registerPackage("org.norm4j.tests.test11");

        metadataManager.createTables(getDataSource());

        tableManager = new TableManager(getDataSource(), metadataManager);

        tenant = new Tenant();

        tenant.setName("Tenant 1");

        tableManager.persist(tenant);

        author = new Author();

        author.setTenantId(tenant.getId());
        author.setName("Author 1");

        tableManager.persist(author);

        book1 = new Book();

        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author.getId());
        book1.setPublishDate(new Date(System.currentTimeMillis()));
        book1.setPriceDate(new Date(System.currentTimeMillis()));
        book1.setPrice(50);

        tableManager.persist(book1);

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setPriceDate(new Date(System.currentTimeMillis()));
        book2.setPrice(100);

        tableManager.persist(book2);
    }

    @Test
    public void test11() {
        List<Book> books;

        books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .where(Book::getPublishDate, "<>", new Date())
                .orderByDesc(Book::getName)
                .getResultList(Book.class);

        assertEquals(0, books.size());

        books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .where(Book::getPriceDate, "<>", new Date())
                .and(q -> q.condition(Book::getPriceDate, ">=", new Date())
                        .or(Functions.coalesce(new Date()), "is", (Object) null))
                .getResultList(Book.class);

        assertEquals(0, books.size());
    }

    @AfterEach
    void cleanup() {
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
