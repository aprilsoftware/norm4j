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
package org.norm4j.tests.test13;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test13 extends BaseTest
{
    private TableManager tableManager;
    private Tenant tenant;
    private Author author1;
    private Author author2;
    private Book book1;
    private Book book2;

    public Test13()
    {
    }

    @BeforeEach
    public void setup()
    {
        MetadataManager metadataManager;

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");

        metadataManager = new MetadataManager();

        metadataManager.registerPackage("org.norm4j.tests.test13");

        metadataManager.createTables(getDataSource());

        tableManager = new TableManager(getDataSource(), metadataManager);

        tenant = new Tenant();

        tenant.setName("Tenant 1");

        tableManager.persist(tenant);

        author1 = new Author();

        author1.setTenantId(tenant.getId());
        author1.setName("Author 1");

        tableManager.persist(author1);

        author2 = new Author();

        author2.setTenantId(tenant.getId());
        author2.setName("Author 2");

        tableManager.persist(author2);

        book1 = new Book();

        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author1.getId());
        book1.setPublishDate(new Date(System.currentTimeMillis()));
        book1.setPriceDate(new Date(System.currentTimeMillis()));
        book1.setPrice(50);

        tableManager.persist(book1);

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author1.getId());
        book2.setSecondAuthorId(author2.getId());
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setPriceDate(new Date(System.currentTimeMillis()));
        book2.setPrice(100);

        tableManager.persist(book2);
    }

    @Test
    public void test13()
    {
        List<Book> books;
        List<Author> authors;

        books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class, Book::getAuthorId)
            .getResultList(Book.class);

        assertEquals(2, books.size());

        books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class, Book::getSecondAuthorId)
            .getResultList(Book.class);

        assertEquals(1, books.size());

        assertEquals(true, books.get(0).getId().equals(book2.getId()));

        authors = tableManager.createSelectQueryBuilder()
                .select(Author.class)
                .from(Author.class)
                .innerJoin(Book.class, Author::getId)
                .groupBy(Author.class)
            .getResultList(Author.class);

        assertEquals(1, authors.size());

        assertEquals(true, authors.get(0).getId().equals(author1.getId()));

        authors = tableManager.createSelectQueryBuilder()
                .select(Author.class)
                .from(Author.class)
                .innerJoin(Book.class, Book::getSecondAuthorId)
                .groupBy(Author.class)
            .getResultList(Author.class);

        assertEquals(1, authors.size());

        assertEquals(true, authors.get(0).getId().equals(author2.getId()));

        books = tableManager.joinMany(author1, Book.class);

        assertEquals(2, books.size());

        books = tableManager.joinMany(author1, Book.class, Book::getAuthorId);

        assertEquals(2, books.size());

        books = tableManager.joinMany(author1, Book.class, Author::getId);

        assertEquals(2, books.size());

        books = tableManager.joinMany(author2, Book.class);

        assertEquals(0, books.size());

        books = tableManager.joinMany(author2, Book.class, Book::getSecondAuthorId);

        assertEquals(1, books.size());

        assertEquals(true, books.get(0).getId().equals(book2.getId()));
    }

    @AfterEach
    void cleanup()
    {
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
