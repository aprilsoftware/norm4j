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

import java.util.ArrayList;
import java.util.List;

import org.norm4j.FieldGetter;
import org.norm4j.TableManager;

public class RecordMapperBuilder<R, D>
{
    private final Class<D> targetClass;
    private final List<FieldMapping<R, D>> fieldMappings;
    private final List<RelationMapping<?, ?, ?>> relationMappings;

    private RecordMapperBuilder(Class<D> targetClass)
    {
        this.targetClass = targetClass;

        fieldMappings = new ArrayList<>();

        relationMappings = new ArrayList<>();
    }

    public static <R, D> RecordMapperBuilder<R, D> from(Class<R> sourceClass, Class<D> targetClass)
    {
        return new RecordMapperBuilder<R,D>(targetClass);
    }

    public <V> BuilderPropertyStep<R, D, V> map(FieldGetter<R, V> sourceGetter)
    {
        return new BuilderPropertyStep<>(this, sourceGetter);
    }

    public <S, T> RelationStep<R, D, S, T, RecordMapperBuilder<R, D>> join(FieldGetter<D, ?> targetGetter, 
            Class<S> sourceClass, 
            Class<T> targetClass)
    {
        return new RelationStep<>(this, null, this, targetGetter, sourceClass, targetClass);
    }

    public RecordMapper<R, D> build()
    {
        if (!relationMappings.isEmpty())
        {
            throw new RuntimeException("Invalid state: you’ve added joins; invoke build(tableManager) instead.");
        }

        return new RecordMapper<>(null,
                targetClass,
                fieldMappings,
                relationMappings);
    }

    public RecordMapper<R, D> build(TableManager tableManager)
    {
        for (RelationMapping<?, ?, ?> relationMapping : relationMappings)
        {
            if (relationMapping.getTargetGetter() == null &&
                    relationMappings.size() != 1)
            {
                throw new RuntimeException("toObject() cannot be combined with other mappings.");
            }
        }

        return new RecordMapper<>(tableManager,
                targetClass,
                fieldMappings,
                relationMappings);
    }

    public static class BuilderPropertyStep<R, D, V>
    {
        private final RecordMapperBuilder<R, D> builder;
        private final FieldGetter<R, V> sourceGetter;

        public BuilderPropertyStep(RecordMapperBuilder<R, D> builder, FieldGetter<R, V> sourceGetter)
        {
            this.builder = builder;
            this.sourceGetter = sourceGetter;
        }

        public RecordMapperBuilder<R, D> to(FieldGetter<D, ?> targetGetter)
        {
            builder.fieldMappings.add(new FieldMapping<>(sourceGetter, targetGetter));

            return builder;
        }
    }

    public static class RelationStep<R, D, S, T, P>
    {
        private final RecordMapperBuilder<R, D> rootBuilder;
        private final RelationStep<?, ?, ?, ?, ?> parentStep;
        private final P parentObject;
        private final FieldGetter<?, ?> targetGetter;
        private final Class<S> sourceClass;
        private final Class<T> targetClass;
        private final List<FieldMapping<S, T>> fieldMappings;
        private final List<RelationMapping<?, ?, ?>> relationMappings;

        private RelationStep(RecordMapperBuilder<R, D> rootBuilder,
                RelationStep<?, ?, ?, ?, ?> parentStep,
                P parentObject,
                FieldGetter<?, ?> targetGetter,
                Class<S> sourceClass,
                Class<T> targetClass)
        {
            this.rootBuilder   = rootBuilder;
            this.parentStep    = parentStep;
            this.parentObject  = parentObject;
            this.targetGetter  = targetGetter;
            this.sourceClass   = sourceClass;
            this.targetClass   = targetClass;

            fieldMappings = new ArrayList<>();

            relationMappings = new ArrayList<>();
        }

        public RelationPropertyStep<R, D, S, T, P> map(FieldGetter<S, ?> sourceGetter)
        {
            return new RelationPropertyStep<>(this, sourceGetter);
        }

        public <S2, T2> RelationStep<R, D, S2, T2, RelationStep<R, D, S, T, P>> join(FieldGetter<T, ?> targetGetter,
                Class<S2> sourceClass,
                Class<T2> targetClass)
        {
            return new RelationStep<>(rootBuilder, this, this, targetGetter, sourceClass, targetClass);
        }

        @SuppressWarnings("unchecked")
        public P endJoin()
        {
            RelationMapping<S, S, T> rm;
            
            
            rm = new RelationMapping<>((FieldGetter<S, ?>) targetGetter, 
                    sourceClass, targetClass, fieldMappings, relationMappings);

            if (parentStep == null)
            {
                rootBuilder.relationMappings.add(rm);
            }
            else
            {
                parentStep.relationMappings.add(rm);
            }

            return parentObject;
        }
    }

    public static class RelationPropertyStep<R, D, S, T, P>
    {
        private final RelationStep<R, D, S, T, P> parent;
        private final FieldGetter<S, ?> sourceGetter;

        private RelationPropertyStep(RelationStep<R, D, S, T, P> parent, 
                FieldGetter<S, ?> sourceGetter)
        {
            this.parent = parent;

            this.sourceGetter = sourceGetter;
        }

        public P toObject()
        {
            parent.fieldMappings.add(new FieldMapping<>(sourceGetter, null));

            return parent.endJoin();
        }

        public RelationStep<R, D, S, T, P> to(FieldGetter<T, ?> targetGetter)
        {
            parent.fieldMappings.add(new FieldMapping<>(sourceGetter, targetGetter));

            return parent;
        }
    }
}
