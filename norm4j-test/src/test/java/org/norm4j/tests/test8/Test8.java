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
package org.norm4j.tests.test8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.norm4j.Query;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

public class Test8 extends BaseTest
{
    public Test8()
    {
    }

    @Test
    public void test8()
    {
        MetadataManager metadataManager;
        TableManager tableManager;
        List<Object[]> objects;
        StringBuilder sql;
        List<Book> books;
        Tenant tenant;
        Author author;
        Book book1;
        Book book2;
        Query query;
        

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

        book1 = new Book();

        book1.setTenantId(tenant.getId());
        book1.setName("Book 1");
        book1.setAuthorId(author.getId());
        book1.setPublishDate(new Date(System.currentTimeMillis()));

        tableManager.persist(book1);

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());
        book2.setBookType(BookType.Documentation);
        book2.setPublishDate(new Date(System.currentTimeMillis()));

        tableManager.persist(book2);

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

        assertEquals(books.size(), 1);

        objects = tableManager.createSelectQueryBuilder()
                .select(Book.class, "b")
                .select(Author.class, "a")
                .select(Author::getName, "a")
                .from(Book.class, "b")
                .where(Book::getTenantId, "b", "=", book2.getTenantId())
                .where(Book::getId, "b", "=", book2.getId())
                .innerJoin(Author.class, "a")
                .orderBy(Book::getName, "b")
                .orderBy(Author::getName, "a")
            .getResultList(new Class<?>[] {Book.class, Author.class});

        assertEquals(objects.size(), 1);

        for (Object[] row : objects)
        {
            assertEquals(row.length, 3);

            for (int i = 0; i < row.length; i++)
            {
                if (i == 0)
                {
                    assertEquals(row[i] instanceof Book, true);
                }
                else if (i == 1)
                {
                    assertEquals(row[i] instanceof Author, true);
                }
                else if (i == 2)
                {
                    assertEquals(row[i] instanceof String, true);
                }
            }
        }
        
        sql = new StringBuilder();
        sql.append("select * from book ");
        sql.append("where tenant_id = ? and id = ?");

        query = tableManager.createQuery(sql.toString());

        query.setParameter(1, book1.getTenantId());
        query.setParameter(2, book1.getId());

        books = query.getResultList(Book.class);

        assertEquals(books.size(), 1);

        sql = new StringBuilder();
        sql.append("select * from book as b ");
        sql.append("inner join author as a on a.tenant_id = b.tenant_id ");
        sql.append("and a.id = b.author_id ");
        sql.append("where b.tenant_id = ? and b.id = ?");

        query = tableManager.createQuery(sql.toString());

        query.setParameter(1, book1.getTenantId());
        query.setParameter(2, book1.getId());

        objects = query.getResultList(new Class<?>[] {Book.class, Author.class});

        assertEquals(objects.size(), 1);

        for (Object[] row : objects)
        {
            assertEquals(row.length, 2);

            for (int i = 0; i < row.length; i++)
            {
                if (i == 0)
                {
                    assertEquals(row[i] instanceof Book, true);
                }
                else if (i == 1)
                {
                    assertEquals(row[i] instanceof Author, true);
                }
            }
        }

        tableManager.remove(book1);
        tableManager.remove(book2);
        tableManager.remove(Author.class, 
                new RowId(author.getTenantId(), author.getId()));

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
