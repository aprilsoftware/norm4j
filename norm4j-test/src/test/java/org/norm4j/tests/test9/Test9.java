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
package org.norm4j.tests.test9;

import org.junit.jupiter.api.Test;
import org.norm4j.TableManager;
import org.norm4j.dialects.SQLServerDialect;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.tests.BaseTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test9 extends BaseTest {
    public Test9() {
    }

    @Test
    public void test9() {
        MetadataManager metadataManager;
        TableManager tableManager;
        Tenant tenant;
        Author author;
        Book book1;
        Book book2;
        long count;
        double value;

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
        book1.setPrice(50);

        tableManager.persist(book1);

        book2 = new Book();

        book2.setTenantId(tenant.getId());
        book2.setName("Book 2");
        book2.setAuthorId(author.getId());
        book2.setPublishDate(new Date(System.currentTimeMillis()));
        book2.setPrice(100);

        tableManager.persist(book2);

        if (getDialect() instanceof SQLServerDialect) {
            count = tableManager.createSelectQueryBuilder()
                    .count()
                    .from(Book.class)
                    .getSingleResult(int.class);

            assertEquals(2, count);

            count = tableManager.createSelectQueryBuilder()
                    .count(Author::getId)
                    .from(Book.class)
                    .innerJoin(Author.class)
                    .where(Book::getTenantId, "=", book1.getTenantId())
                    .where(Book::getId, "=", book1.getId())
                    .getSingleResult(int.class);

            assertEquals(1, count);
        } else {
            count = tableManager.createSelectQueryBuilder()
                    .count()
                    .from(Book.class)
                    .getSingleResult(long.class);

            assertEquals(2, count);

            count = tableManager.createSelectQueryBuilder()
                    .count(Author::getId)
                    .from(Book.class)
                    .innerJoin(Author.class)
                    .where(Book::getTenantId, "=", book1.getTenantId())
                    .where(Book::getId, "=", book1.getId())
                    .getSingleResult(long.class);

            assertEquals(1, count);
        }

        value = tableManager.createSelectQueryBuilder()
                .sum(Book::getPrice)
                .from(Book.class)
                .getSingleResult(double.class);

        assertEquals(150, value);

        value = tableManager.createSelectQueryBuilder()
                .sum(Book::getPrice)
                .from(Book.class)
                .innerJoin(Author.class)
                .groupBy(Book::getName)
                .orderByDesc(Book::getName)
                .getSingleResult(double.class);

        assertEquals(100, value);

        value = tableManager.createSelectQueryBuilder()
                .avg(Book::getPrice)
                .from(Book.class)
                .getSingleResult(double.class);

        assertEquals(75, value);

        value = tableManager.createSelectQueryBuilder()
                .min(Book::getPrice)
                .from(Book.class)
                .getSingleResult(double.class);

        assertEquals(50, value);

        value = tableManager.createSelectQueryBuilder()
                .max(Book::getPrice)
                .from(Book.class)
                .getSingleResult(double.class);

        assertEquals(100, value);

        tableManager.remove(book1);
        tableManager.remove(book2);
        tableManager.remove(Author.class,
                new RowId(author.getTenantId(), author.getId()));

        dropTable(null, "book");
        dropTable(null, "author");
        dropTable(null, "tenant");
    }
}
