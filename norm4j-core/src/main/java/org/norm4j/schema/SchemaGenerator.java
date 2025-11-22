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

import org.norm4j.metadata.MetadataManager;
import org.norm4j.metadata.TableMetadata;

public class SchemaGenerator {
    private final MetadataManager metadataManager;

    public SchemaGenerator(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public Schema generate(String version) {
        Schema schema = new Schema();
        schema.setVersion(version);
        schema.setSchemaModelVersion(1);

        for (TableMetadata tableMetadata : metadataManager.getTableMetadata()) {
            schema.getTables().add(new SchemaTable(tableMetadata));
        }

        return schema;
    }
}
