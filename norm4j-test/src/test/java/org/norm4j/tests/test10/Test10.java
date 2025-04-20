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
package org.norm4j.tests.test10;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.norm4j.Functions;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test10 extends BaseTest {

    private TableManager tableManager;
    private Book book1 = new Book();
    private Book book2 = new Book();

    @BeforeEach
    void setup() {
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");

        MetadataManager metadataManager = new MetadataManager();
        metadataManager.registerTable(Tenant.class);
        metadataManager.registerTable(Book.class);
        metadataManager.registerTable(Author.class);
        metadataManager.createTables(getDataSource());

        tableManager = new TableManager(getDataSource(), metadataManager);
        setupTestData();
    }

    private void setupTestData() {
        var tenant = new Tenant();
        tenant.setName("Tenant 1");
        tableManager.persist(tenant);

        var author = new Author();
        author.setTenantId(tenant.getId());
        author.setName("Author 1");
        tableManager.persist(author);

        book1 = new Book();
        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author.getId());
        book1.setPublishDate(new Date(System.currentTimeMillis()));
        book1.setPrice(50);

        book2 = new Book();
        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());
        book2.setBookType(BookType.Roman);
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setPrice(100);



        tableManager.persist(book1);
        tableManager.persist(book2);
    }

    @Test
    void testSelectBooksByIds() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(q -> q.condition(Book::getId, "=", book1.getId())
                        .or(Book::getId, "=", book2.getId()))
                .orderByDesc(Book::getName)
                .getResultList(Book.class);
        assertEquals(2, books.size());
    }

    @Test
    void testCoalesceWithNullFallback() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(Functions.coalesce(null, "Roman"), "=", "Roman")
                .getResultList(Book.class);
        assertEquals(2, books.size());
    }

    @Test
    void testCoalesceWithNonMatchingValueReturnsEmpty() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(Functions.coalesce("Documentation"),
                        "=", "Roman")
                .orderByDesc(Book::getName)
                .getResultList(Book.class);

        assertEquals(0, books.size());
    }

    @Test
    void testCoalesceWithArgumentsReturnsEmpty() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(Functions.coalesce(),
                        "=", "Roman")
                .orderByDesc(Book::getName)
                .getResultList(Book.class);

        assertEquals(0, books.size());
    }

    @Test
    void testFilterByBookType() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(Book::getBookType, "=", book2.getBookType())
                .orderByDesc(Book::getName)
                .getResultList(Book.class);

        assertEquals(1, books.size());
    }

    @Test
    void testUpdateBookType() {
        tableManager.createUpdateQueryBuilder()
                .update(Book.class)
                .set(Book::getBookType, BookType.Documentation)
                .where(Book::getId, "=", book1.getId())
                .executeUpdate();

        book1.setBookType(tableManager.createSelectQueryBuilder()
                .select(Book::getBookType)
                .from(Book.class)
                .where(Book::getId, "=", book1.getId())
                .getSingleResult(BookType.class));

        assertEquals(BookType.Documentation, book1.getBookType());
    }

    @Test
    void testSelectBooksWithBookTypeInEnum() {
        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .where(Book::getBookType, "in", BookType.values())
                .orderByDesc(Book::getName)
                .getResultList(Book.class);

        assertEquals(1, books.size());
    }

    @Test
    void testDeleteBookById() {
        tableManager.createDeleteQueryBuilder()
                .from(Book.class)
                .where(Book::getId, "=", book1.getId())
                .executeUpdate();

        var books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .getResultList(Book.class);

        assertEquals(1, books.size());
    }

    @AfterEach
    void cleanup() {
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
