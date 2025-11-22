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
import java.util.List;

import org.norm4j.Array;
import org.norm4j.Column;
import org.norm4j.Enumerated;
import org.norm4j.GeneratedValue;
import org.norm4j.Id;
import org.norm4j.SequenceGenerator;
import org.norm4j.TableGenerator;
import org.norm4j.Temporal;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.schema.annotations.Annotation;
import org.norm4j.schema.annotations.ArrayAnnotation;
import org.norm4j.schema.annotations.ColumnAnnotation;
import org.norm4j.schema.annotations.EnumeratedAnnotation;
import org.norm4j.schema.annotations.GeneratedValueAnnotation;
import org.norm4j.schema.annotations.IdAnnotation;
import org.norm4j.schema.annotations.SequenceGeneratorAnnotation;
import org.norm4j.schema.annotations.TableGeneratorAnnotation;
import org.norm4j.schema.annotations.TemporalAnnotation;

public class SchemaColumn {
    private String fieldName;
    private String fieldType;
    private List<Annotation> annotations;

    public SchemaColumn() {
        annotations = new ArrayList<>();
    }

    public SchemaColumn(ColumnMetadata columnMetadata) {
        annotations = new ArrayList<>();

        fieldName = columnMetadata.getField().getName();
        fieldType = columnMetadata.getField().getType().getName();

        Array arrayAnnotation;

        arrayAnnotation = (Array) columnMetadata.getAnnotations().get(Array.class);

        if (arrayAnnotation != null) {
            annotations.add(new ArrayAnnotation(arrayAnnotation));
        }

        Column columnAnnotation;

        columnAnnotation = (Column) columnMetadata.getAnnotations().get(Column.class);

        if (columnAnnotation != null) {
            annotations.add(new ColumnAnnotation(columnAnnotation));
        }

        Enumerated enumeratedAnnotation;

        enumeratedAnnotation = (Enumerated) columnMetadata.getAnnotations().get(Enumerated.class);

        if (enumeratedAnnotation != null) {
            annotations.add(new EnumeratedAnnotation(enumeratedAnnotation));
        }

        GeneratedValue generatedValueAnnotation;

        generatedValueAnnotation = (GeneratedValue) columnMetadata.getAnnotations().get(GeneratedValue.class);

        if (generatedValueAnnotation != null) {
            annotations.add(new GeneratedValueAnnotation(generatedValueAnnotation));
        }

        Id idValueAnnotation;

        idValueAnnotation = (Id) columnMetadata.getAnnotations().get(Id.class);

        if (idValueAnnotation != null) {
            annotations.add(new IdAnnotation());
        }

        SequenceGenerator sequenceGeneratorAnnotation;

        sequenceGeneratorAnnotation = (SequenceGenerator) columnMetadata.getAnnotations()
                .get(SequenceGenerator.class);

        if (sequenceGeneratorAnnotation != null) {
            annotations.add(new SequenceGeneratorAnnotation(sequenceGeneratorAnnotation));
        }

        TableGenerator tableGeneratorAnnotation;

        tableGeneratorAnnotation = (TableGenerator) columnMetadata.getAnnotations()
                .get(TableGenerator.class);

        if (tableGeneratorAnnotation != null) {
            annotations.add(new TableGeneratorAnnotation(tableGeneratorAnnotation));
        }

        Temporal temporalAnnotation;

        temporalAnnotation = (Temporal) columnMetadata.getAnnotations()
                .get(Temporal.class);

        if (temporalAnnotation != null) {
            annotations.add(new TemporalAnnotation(temporalAnnotation));
        }
    }

    public String getColumnName() {
        ColumnAnnotation columnAnnotation;

        columnAnnotation = Annotation.get(this, ColumnAnnotation.class);

        if (columnAnnotation == null ||
                columnAnnotation.getName().isEmpty()) {
            return fieldName;
        } else {
            return columnAnnotation.getName();
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
}
