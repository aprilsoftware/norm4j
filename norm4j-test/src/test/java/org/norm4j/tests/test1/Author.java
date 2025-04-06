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
package org.norm4j.tests.test1;

import org.norm4j.Column;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Id;
import org.norm4j.Table;
import org.norm4j.TableGenerator;

@Table(name = "author", schema = "test1")
public class Author
{
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @TableGenerator(schema = "test1",
            table = "norm_sequences2", 
            pkColumnName = "sequence", 
            valueColumnName = "value"
    )
    private int id;

    @Column(nullable = false)
    private String name;

    public Author()
    {
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
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

    @Override
    public int hashCode()
    {
        final int prime = 31;

        int result = 1;

        result = prime * result + id;

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

        Author other = (Author) obj;

        return id == other.id;
    }
}
