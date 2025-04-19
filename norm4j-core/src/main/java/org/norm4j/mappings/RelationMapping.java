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
package org.norm4j.mappings;

import java.util.List;

import org.norm4j.FieldGetter;

public class RelationMapping<P, S, T>
{
    private final FieldGetter<P, ?> targetGetter;
    private final Class<S> sourceClass;
    private final Class<T> targetClass;
    private final List<FieldMapping<S, T>> fieldMappings;
    private final List<RelationMapping<T, ?, ?>> childRelationMappings;
    
    public RelationMapping(FieldGetter<P, ?> targetGetter,
            Class<S> sourceClass,
            Class<T> targetClass,
            List<FieldMapping<S, T>> fieldMappings,
            List<RelationMapping<?, ?, ?>> childRelationMappings)
    {
        this.targetGetter = targetGetter;

        this.sourceClass = sourceClass;

        this.targetClass = targetClass;

        this.fieldMappings = fieldMappings;

        @SuppressWarnings("unchecked")
        List<RelationMapping<T, ?, ?>> relations = (List<RelationMapping<T, ?, ?>>)
                (List<?>) childRelationMappings;

        this.childRelationMappings = relations;
    }

    public FieldGetter<P, ?> getTargetGetter()
    {
        return targetGetter;
    }

    public Class<S> getSourceClass()
    {
        return sourceClass;
    }

    public Class<T> getTargetClass()
    {
        return targetClass;
    }

    public List<FieldMapping<S, T>> getFieldMappings()
    {
        return fieldMappings;
    }

    public List<RelationMapping<T, ?, ?>> getChildRelationMappings()
    {
        return childRelationMappings;
    }
}
