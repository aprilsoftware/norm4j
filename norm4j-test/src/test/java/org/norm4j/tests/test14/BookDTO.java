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

import java.util.Date;
import java.util.UUID;

public class BookDTO
{
    private UUID tenantId;
    private UUID id;
    private String name;
    private AuthorDTO author;
    private Date publishDate;
    private Date priceDate;

    public BookDTO()
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

    public AuthorDTO getAuthor()
    {
        return author;
    }

    public void setAuthor(AuthorDTO author)
    {
        this.author = author;
    }

    public Date getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(Date publishDate)
    {
        this.publishDate = publishDate;
    }

    public Date getPriceDate()
    {
        return priceDate;
    }

    public void setPriceDate(Date priceDate)
    {
        this.priceDate = priceDate;
    }
}
