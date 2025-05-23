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
package org.norm4j.tests.test5;

import org.junit.jupiter.api.Test;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Test5 extends BaseTest {
    public Test5() {
    }

    @Test
    public void test5() {
        MetadataManager metadataManager;
        TableManager tableManager;
        List<Book> books;
        Tenant tenant;
        Author author;
        Book book1;
        Book book2;

        dropTable("test5", "book");
        dropTable("test5", "author");
        dropTable("test5", "tenant");

        metadataManager = new MetadataManager();

        metadataManager.registerTable(Tenant.class);
        metadataManager.registerTable(Book.class);
        metadataManager.registerTable(Author.class);

        metadataManager.createTables(getDataSource());

        tableManager = new TableManager(getDataSource(), metadataManager);

        tenant = new Tenant();

        tenant.setName("Tenant 1");

        tableManager.persist(tenant);

        author = new Author();

        author.setTenantId(tenant.getId());
        author.setName("Author 1");

        tableManager.persist(author);

        author = tableManager.find(Author.class,
                new RowId(author.getTenantId(), author.getId()));

        assertNotEquals(null, author);

        book1 = new Book();

        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author.getId());

        tableManager.persist(book1);

        author.setName("Author 1.1");

        tableManager.merge(author);

        author = tableManager.joinOne(book1, Author.class);

        assertNotEquals(null, author);

        books = tableManager.joinMany(author, Book.class);

        assertEquals(1, books.size());

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());

        tableManager.persist(book2);

        books = tableManager.joinMany(author, Book.class);

        assertEquals(2, books.size());

        books = tableManager.createSelectQueryBuilder()
                .select(Book.class)
                .from(Book.class)
                .innerJoin(Author.class)
                .where(Book::getTenantId, "=", author.getTenantId())
                .where(Book::getAuthorId, "=", author.getId())
                .orderBy(Book::getName)
                .orderBy(Author::getName)
                .limit(1)
                .getResultList(Book.class);

        assertEquals(1, books.size());

        tableManager.remove(book1);
        tableManager.remove(book2);
        tableManager.remove(Author.class,
                new RowId(author.getTenantId(), author.getId()));

        dropTable("test5", "book");
        dropTable("test5", "author");
        dropTable("test5", "tenant");
    }
}
