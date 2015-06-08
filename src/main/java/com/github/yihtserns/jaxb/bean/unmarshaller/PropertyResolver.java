/*
 * Copyright 2015 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.jaxb.bean.unmarshaller;

import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author yihtserns
 */
abstract class PropertyResolver<T extends AccessibleObject> {

    public static final PropertyResolver<Method> METHOD = new PropertyResolver<Method>() {
        @Override
        public AccessibleObject[] getDirectMembers(Class<?> type) {
            return type.getDeclaredMethods();
        }

        public String getPropertyName(Method method) {
            String propertyName = method.getName();
            if (propertyName.startsWith("is")) {
                propertyName = propertyName.substring(2);
            } else {
                // Assume is setXXX/getXXX
                propertyName = propertyName.substring(3);
            }
            return Introspector.decapitalize(propertyName);
        }

        public Class<?> getPropertyType(Method method) {
            return isSetter(method) ? method.getParameterTypes()[0] : method.getReturnType();
        }

        @Override
        public Type getGenericType(Method method) {
            return isSetter(method) ? method.getGenericParameterTypes()[0] : method.getGenericReturnType();
        }

        private boolean isSetter(Method method) {
            return method.getName().startsWith("set");
        }
    };
    static final PropertyResolver<Field> FIELD = new PropertyResolver<Field>() {
        @Override
        public AccessibleObject[] getDirectMembers(Class<?> type) {
            return type.getDeclaredFields();
        }

        public String getPropertyName(Field field) {
            return field.getName();
        }

        public Class<?> getPropertyType(Field field) {
            return field.getType();
        }

        @Override
        public Type getGenericType(Field field) {
            return field.getGenericType();
        }
    };

    public abstract AccessibleObject[] getDirectMembers(Class<?> type);

    public abstract String getPropertyName(T t);

    public abstract Class<?> getPropertyType(T t);

    public abstract Type getGenericType(T t);

    public Class<?> getListComponentType(T t) {
        Type type = getGenericType(t);
        type = ((ParameterizedType) type).getActualTypeArguments()[0];

        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }

        return (Class) type;
    }
}
