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
package org.norm4j.tests.test2;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test2 extends BaseTest
{
    public Test2()
    {
    }

    @Test
    public void test2()
    {
        MetadataManager metadataManager;
        TableManager tableManager;
        Author author;
        Book book;

        dropTable("test2", "book");
        dropTable("test2", "author");

        metadataManager = new MetadataManager();

        metadataManager.registerTable(Book.class);
        metadataManager.registerTable(Author.class);

        metadataManager.createTables(getDataSource());

        tableManager = new TableManager(getDataSource(), metadataManager);

        author = new Author();

        author.setName("Author 1");

        tableManager.persist(author);

        author = tableManager.find(Author.class, author.getId());

        assertNotEquals(author, null);

        book = new Book();

        book.setName("Book 1");
        book.setAuthorId(author.getId());

        tableManager.persist(book);

        author.setName("Author 1.1");

        tableManager.merge(author);

        tableManager.remove(book);
        tableManager.remove(Author.class, 1);

        dropTable("test2", "book");
        dropTable("test2", "author");
    }
}
