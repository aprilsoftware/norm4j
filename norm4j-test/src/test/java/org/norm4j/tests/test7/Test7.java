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
package org.norm4j.tests.test7;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test7 extends BaseTest
{
    public Test7()
    {
    }

    @Test
    public void test7()
    {
        MetadataManager metadataManager;
        TableManager tableManager;
        List<Book> books;
        Tenant tenant;
        Author author;
        Book book1;
        Book book2;

        if (!isArraySupported())
        {
            return;
        }

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");

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

        assertNotEquals(author, null);

        book1 = new Book();

        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author.getId());
        book1.setPublishDate(new Date(System.currentTimeMillis()));

        tableManager.persist(book1);

        author.setName("Author 1.1");

        tableManager.merge(author);

        author = tableManager.joinOne(book1, Author.class);

        assertNotEquals(author, null);

        books = tableManager.joinMany(author, Book.class);

        assertEquals(books.size(), 1);

        books = tableManager.joinMany(author, Author::getId, 
                Book.class, 
                Book::getAuthorId);

        assertEquals(books.size(), 1);

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());
        book2.setBookType(BookType.Documentation);
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setEmbedding(new float[] {0.1F, 0.2F, -0.1F});
        book2.setIds1(new int[] {1, 2});
        book2.setIds2(new String[] { "s1", "s2"});

        tableManager.persist(book2);

        books = tableManager.joinMany(author, Book.class);

        assertEquals(books.size(), 2);

        books = tableManager.joinMany(author, Author::getId, 
                Book.class, 
                Book::getAuthorId);

        assertEquals(books.size(), 2);

        tableManager.remove(book1);
        tableManager.remove(book2);
        tableManager.remove(Author.class, 
                new RowId(author.getTenantId(), author.getId()));

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
