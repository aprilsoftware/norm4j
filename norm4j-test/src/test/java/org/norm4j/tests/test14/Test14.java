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
package org.norm4j.tests.test14;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.norm4j.TableManager;
import org.norm4j.mappings.RecordMapper;
import org.norm4j.mappings.RecordMapperBuilder;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test14 extends BaseTest {
    private TableManager tableManager;
    private Tenant tenant;
    private Author author1;
    private Author author2;
    private Book book1;
    private Book book2;

    public Test14() {
    }

    @BeforeEach
    public void setup() {
        MetadataManager metadataManager;

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");

        metadataManager = new MetadataManager();

        metadataManager.registerPackage("org.norm4j.tests.test14");

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
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setPriceDate(new Date(System.currentTimeMillis()));
        book2.setPrice(100);

        tableManager.persist(book2);
    }

    @Test
    public void test14() {
        RecordMapper<Book, BookDTO> bookMapper;
        RecordMapper<Author, AuthorDTO> authorMapper;
        List<BookDTO> booksDTO;
        List<Book> books;
        AuthorDTO authorDTO;
        BookDTO bookDTO;

        // basic mapping
        authorDTO = RecordMapper.from(Author.class,
                AuthorDTO.class).map(author1);

        assertEquals(true, authorDTO.getId().equals(author1.getId()));

        // authorMapper
        authorMapper = RecordMapperBuilder.from(Author.class, AuthorDTO.class)
                .map(Author::getId).to(AuthorDTO::getId)
                .join(AuthorDTO::getBooks, Book.class, BookDTO.class)
                .endJoin()
                .join(AuthorDTO::getBookIds, Book.class, UUID.class)
                .map(Book::getId).toObject()
                .build(tableManager);

        authorDTO = authorMapper.map(author1);

        assertEquals(2, authorDTO.getBooks().size());

        assertEquals(2, authorDTO.getBookIds().size());

        authorDTO = authorMapper.map(author2);

        assertEquals(0, authorDTO.getBooks().size());

        // bookMapper
        bookMapper = RecordMapperBuilder.from(Book.class, BookDTO.class)
                .join(BookDTO::getAuthor, Author.class, AuthorDTO.class)
                .join(AuthorDTO::getBooks, Book.class, BookDTO.class)
                .endJoin()
                .endJoin()
                .build(tableManager);

        bookDTO = bookMapper.map(book1);

        assertEquals(true, bookDTO.getAuthor().getId().equals(author1.getId()));

        books = tableManager.createSelectQueryBuilder()
                .select()
                .from(Book.class)
                .getResultList(Book.class);

        booksDTO = bookMapper.mapList(books);

        assertEquals(2, booksDTO.size());

        for (BookDTO b : booksDTO) {
            assertEquals(2, b.getAuthor().getBooks().size());

            assertEquals(0, b.getAuthor().getBookIds().size());
        }
    }

    @AfterEach
    void cleanup() {
        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
