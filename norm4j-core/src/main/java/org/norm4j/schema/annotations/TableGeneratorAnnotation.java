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
package org.norm4j.schema.annotations;

import org.norm4j.TableGenerator;

public class TableGeneratorAnnotation implements Annotation {
    private String table;
    private String schema;
    private String pkColumnName;
    private String valueColumnName;
    private int initialValue;

    public TableGeneratorAnnotation() {
    }

    public TableGeneratorAnnotation(TableGenerator tableGenerator) {
        this.table = tableGenerator.table();
        this.schema = tableGenerator.schema();
        this.pkColumnName = tableGenerator.pkColumnName();
        this.valueColumnName = tableGenerator.valueColumnName();
        this.initialValue = tableGenerator.initialValue();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String name) {
        this.table = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public void setPkColumnName(String pkColumnName) {
        this.pkColumnName = pkColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public int getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }
}
