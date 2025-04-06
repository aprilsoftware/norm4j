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

import org.norm4j.*;

import java.util.Date;
import java.util.UUID;

@Table
@Join
(
    columns = "tenant_id", 
    reference = @Reference(table = Tenant.class, 
            columns = "id")
)
@Join
(
    name = "test_ref",
    columns = {"tenant_id", "author_id"}, 
    reference = @Reference(table = Author.class, 
            columns = {"tenant_id", "id"})
)
@IdClass(value = RowId.class)
public class Book
{
    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    
    @Column(name = "author_id")
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    private BookType bookType;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date publishDate;

    private double price;

    public Book()
    {
    }

    public UUID getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(UUID tenantId)
    {
        this.tenantId = tenantId;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public UUID getAuthorId()
    {
        return authorId;
    }

    public void setAuthorId(UUID authorId)
    {
        this.authorId = authorId;
    }

    public BookType getBookType()
    {
        return bookType;
    }

    public void setBookType(BookType bookType)
    {
        this.bookType = bookType;
    }

    public Date getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(Date publishDate)
    {
        this.publishDate = publishDate;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;

        int result = 1;

        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());

        result = prime * result + ((id == null) ? 0 : id.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Book other = (Book) obj;

        if (tenantId == null)
        {
            if (other.tenantId != null)
                return false;
        }
        else if (!tenantId.equals(other.tenantId))
            return false;

        if (id == null)
        {
            return other.id == null;
        }
        else return id.equals(other.id);
    }
}
