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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Schema {
    private String version;
    private int schemaModelVersion;
    private List<SchemaTable> tables;

    public Schema() {
        tables = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSchemaModelVersion() {
        return schemaModelVersion;
    }

    public void setSchemaModelVersion(int schemaModelVersion) {
        this.schemaModelVersion = schemaModelVersion;
    }

    public List<SchemaTable> getTables() {
        return tables;
    }

    public void setTables(List<SchemaTable> tables) {
        this.tables = tables;
    }

    public static Schema loadFromResource(String resourcePath) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new RuntimeException("Schema not found: " + resourcePath);
            }

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(is, Schema.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema from: " + resourcePath, e);
        }
    }

    public void write(Path path) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (OutputStream os = Files.newOutputStream(path)) {
            mapper.writeValue(os, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write schema: " + path.toString(), e);
        }
    }
}
