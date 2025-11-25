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
package org.norm4j.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.norm4j.Join;

public class SchemaJoin {
    private String name;
    private List<String> columns;
    private SchemaReference reference;
    private boolean referencialIntegrity;
    private boolean cascadeDelete;

    public SchemaJoin() {
        columns = new ArrayList<>();
    }

    public SchemaJoin(Join join) {
        this.name = join.name();
        this.columns = Arrays.asList(join.columns());
        this.reference = new SchemaReference(join.reference());
        this.referencialIntegrity = join.referencialIntegrity();
        this.cascadeDelete = join.cascadeDelete();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public SchemaReference getReference() {
        return reference;
    }

    public void setReference(SchemaReference reference) {
        this.reference = reference;
    }

    public boolean isReferencialIntegrity() {
        return referencialIntegrity;
    }

    public void setReferencialIntegrity(boolean referencialIntegrity) {
        this.referencialIntegrity = referencialIntegrity;
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }
}
