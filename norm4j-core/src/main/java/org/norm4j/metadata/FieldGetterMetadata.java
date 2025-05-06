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
package org.norm4j.metadata;

import java.lang.reflect.Method;

import org.norm4j.FieldGetter;
import static org.norm4j.metadata.helpers.TableCreationHelper.decapitalize;

public class FieldGetterMetadata {
    private final Class<?> tableClass;
    private final String fieldName;

    public FieldGetterMetadata(Class<?> tableClass, String fieldName) {
        this.tableClass = tableClass;

        this.fieldName = fieldName;
    }

    public Class<?> getTableClass() {
        return tableClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static <T, R> FieldGetterMetadata extractMetadata(FieldGetter<T, R> getter) {
        try {
            Method writeReplace = getter.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object serializedLambda = writeReplace.invoke(getter);

            Method getImplClassMethod = serializedLambda.getClass().getDeclaredMethod("getImplClass");
            Method getImplMethodNameMethod = serializedLambda.getClass().getDeclaredMethod("getImplMethodName");

            String internalClassName = (String) getImplClassMethod.invoke(serializedLambda);
            String className = internalClassName.replace('/', '.');

            String methodName = (String) getImplMethodNameMethod.invoke(serializedLambda);
            String fieldName;

            if (methodName.startsWith("get") && methodName.length() > 3) {
                fieldName = decapitalize(methodName.substring(3));
            } else if (methodName.startsWith("is") && methodName.length() > 2) {
                fieldName = decapitalize(methodName.substring(2));
            } else {
                fieldName = methodName;
            }

            return new FieldGetterMetadata(Class.forName(className), fieldName);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to extract field metadata from getter: " + getter, e);
        }
    }
}
