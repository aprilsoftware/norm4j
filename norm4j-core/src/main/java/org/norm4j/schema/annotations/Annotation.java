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

import org.norm4j.schema.SchemaColumn;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ArrayAnnotation.class, name = "array"),
        @JsonSubTypes.Type(value = ColumnAnnotation.class, name = "column"),
        @JsonSubTypes.Type(value = EnumeratedAnnotation.class, name = "enumerated"),
        @JsonSubTypes.Type(value = GeneratedValueAnnotation.class, name = "generatedValue"),
        @JsonSubTypes.Type(value = IdAnnotation.class, name = "id"),
        @JsonSubTypes.Type(value = SequenceGeneratorAnnotation.class, name = "sequenceGenerator"),
        @JsonSubTypes.Type(value = TableGeneratorAnnotation.class, name = "tableGenerator"),
        @JsonSubTypes.Type(value = TemporalAnnotation.class, name = "temporal")
})
public interface Annotation {
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T get(SchemaColumn column, Class<T> type) {
        if (column.getAnnotations() == null)
            return null;

        for (Annotation annotation : column.getAnnotations()) {
            if (type.isInstance(annotation)) {
                return (T) annotation;
            }
        }
        return null;
    }

    public static boolean has(SchemaColumn column, Class<? extends Annotation> type) {
        return get(column, type) != null;
    }
}
