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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.norm4j.FieldGetter;
import org.norm4j.Join;
import org.norm4j.TableManager;
import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.FieldGetterMetadata;
import org.norm4j.metadata.TableMetadata;

public class RecordMapper<R, D>
{
    private final TableManager tableManager;
    private final Class<D> targetClass;
    private final List<FieldMapping<R, D>> fieldMappings;
    private final List<RelationMapping<?, ?, ?>> relationMappings;

    public RecordMapper(TableManager tableManager,
            Class<D> targetClass,
            List<FieldMapping<R, D>> fieldMappings,
            List<RelationMapping<?, ?, ?>> relationMappings)
    {
        this.tableManager = tableManager;

        this.targetClass = targetClass;

        this.fieldMappings = fieldMappings;

        this.relationMappings = relationMappings;
    }

    public List<D> mapList(List<R> sourceObjects)
    {
        return mapList(sourceObjects, true);
    }

    public List<D> mapList(List<R> sourceObjects, boolean excludeInternal)
    {
        List<D> targetObjects;

        // TODO N+1 optimization
        targetObjects = new ArrayList<>();

        for (R sourceObject : sourceObjects)
        {
            targetObjects.add(map(sourceObject, excludeInternal));
        }

        return targetObjects;
    }

    public D map(R sourceObject)
    {
        return map(sourceObject, true);
    }

    public D map(R sourceObject, boolean excludeInternal)
    {
        D targetObject;

        if (sourceObject == null)
        {
            return null;
        }

        targetObject = map(sourceObject, 
                targetClass, 
                fieldMappings,
                excludeInternal);

        for (RelationMapping<?, ?, ?> relationMapping : relationMappings)
        {
            mapRelation(sourceObject, 
                    targetObject, 
                    relationMapping,
                    excludeInternal);
        }

        return targetObject;
    }

    private <R2, D2, S, T> void mapRelation(R2 sourceObject, D2 targetObject, 
            RelationMapping<?, S, T> relationMapping,
            boolean excludeInternal)
    {
        Field targetField;

        targetField = extractField(relationMapping.getTargetGetter());

        targetField.setAccessible(true);

        if (Collection.class.isAssignableFrom(targetField.getType()))
        {
            List<S> relationObjects;
            List<T> targetValues;

            if (!List.class.isAssignableFrom(targetField.getType()))
            {
                throw new RuntimeException("Invalid value for the relation: only list are supported for now.");
            }

            targetValues = new ArrayList<>();

            relationObjects = tableManager.joinMany(sourceObject, relationMapping.getSourceClass());

            // TODO N+1 optimization
            for (S relationObject : relationObjects)
            {
                T targetValue;

                targetValue = mapTarget(relationObject, relationMapping, excludeInternal);

                targetValues.add(targetValue);
            }

            try
            {
                targetField.set(targetObject, targetValues);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            S relationObject;
            T targetValue;

            relationObject = tableManager.joinOne(sourceObject, 
                    relationMapping.getSourceClass());

            if (relationObject != null)
            {
                targetValue = mapTarget(relationObject, relationMapping, excludeInternal);

                try
                {
                    targetField.set(targetObject, targetValue);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private <S, T> T mapTarget(S relationObject, 
            RelationMapping<?, S, T> relationMapping, 
            boolean excludeInternal)
    {
        T targetValue;

        targetValue = map(relationObject, 
                relationMapping.getTargetClass(), 
                relationMapping.getFieldMappings(),
                excludeInternal);

        for (RelationMapping<?, ?, ?> childMapping : relationMapping.getChildRelations())
        {
            mapRelation(relationObject, 
                    targetValue, 
                    childMapping,
                    excludeInternal);
        }

        return targetValue;
    }

    @SuppressWarnings("unchecked")
    private <S, T> T map(S sourceObject, 
            Class<T> targetClass, 
            List<FieldMapping<S, T>> fieldMappings,
            boolean excludeInternal)
    {
        T targetObject;

        if (fieldMappings.size() == 1 && 
                fieldMappings.get(0).getTargetGetter() == null)
        {
            FieldMapping<S, T> fieldMapping;
            Object value;

            fieldMapping = fieldMappings.get(0);

            value = fieldMapping.getSourceGetter().apply(sourceObject);

            return (T)convertObject(value, targetClass);
        }
        else
        {
            Set<String> explicitFieldNames;

            explicitFieldNames = new HashSet<>();

            try
            {
                targetObject = targetClass.getDeclaredConstructor().newInstance();
    
                for (FieldMapping<S, T> fieldMapping : fieldMappings)
                {
                    Field sourceField;
                    Field targetField;
                    Object value;
    
                    sourceField = extractField(fieldMapping.getSourceGetter());
    
                    if (sourceField.getType().isAnnotationPresent(Ignore.class))
                    {
                        continue;
                    }
    
                    if (excludeInternal &&
                            sourceField.getType().isAnnotationPresent(Internal.class))
                    {
                        continue;
                    }
    
                    explicitFieldNames.add(sourceField.getName());
    
                    value = fieldMapping.getSourceGetter().apply(sourceObject);

                    targetField = extractField(fieldMapping.getTargetGetter());
    
                    targetField.setAccessible(true);
        
                    targetField.set(targetObject, convertObject(value, targetField.getType()));
                }
    
                for (Field sourceField : sourceObject.getClass().getDeclaredFields())
                {
                    String fieldName;
                    Field targetField;
                    Object value;
    
                    if (Modifier.isStatic(sourceField.getModifiers()) || 
                            sourceField.isSynthetic())
                    {
                        continue;
                    }
    
                    if (sourceField.getType().isAnnotationPresent(Ignore.class))
                    {
                        continue;
                    }
    
                    if (excludeInternal &&
                            sourceField.getType().isAnnotationPresent(Internal.class))
                    {
                        continue;
                    }

                    fieldName = sourceField.getName();
    
                    if (explicitFieldNames.contains(fieldName))
                    {
                        continue;
                    }

                    try
                    {
                        targetField = targetClass.getDeclaredField(fieldName);
                    }
                    catch (Exception e)
                    {
                        continue;
                    }

                    if (Modifier.isStatic(targetField.getModifiers()) || 
                            targetField.isSynthetic())
                    {
                        continue;
                    }
    
                    sourceField.setAccessible(true);

                    value = sourceField.get(sourceObject);

                    value = convertObject(value, targetField.getType());

                    targetField.setAccessible(true);

                    targetField.set(targetObject, value);
                }
    
                return targetObject;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private Field extractField(FieldGetter<?, ?> fieldGetter)
    {
        FieldGetterMetadata fieldGetterMetadata;

        fieldGetterMetadata = FieldGetterMetadata.extractMetadata(fieldGetter);

        try
        {
            return fieldGetterMetadata.getTableClass()
                    .getDeclaredField(fieldGetterMetadata.getFieldName());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object convertObject(Object sourceObject, Class<?> targetClass)
    {
        if (sourceObject != null)
        {
            if ((sourceObject instanceof java.sql.Date ||
                    sourceObject instanceof java.util.Date) &&
                    targetClass.equals(String.class))
            {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .format(sourceObject);
            }
            else if (sourceObject instanceof UUID &&
                    targetClass.equals(String.class))
            {
                return sourceObject.toString();
            }
            else if (sourceObject.getClass().isEnum())
            {
                if (targetClass.isEnum())
                {
                    return Enum.valueOf((Class<Enum>)targetClass, 
                            ((Enum<?>)sourceObject).name());
                }
                else
                {
                    return ((Enum<?>)sourceObject).name();
                }
            }
        }

        return sourceObject;
    }

    public static <R, D> RecordMapper<R, D> from(Class<R> sourceClass, Class<D> targetClass)
    {
        return new RecordMapper<>(null,
                targetClass, 
                new ArrayList<>(), 
                new ArrayList<>());
    }
}
