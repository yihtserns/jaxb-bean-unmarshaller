/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.camel.spring2;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author yihtserns
 */
public class JaxbUnmarshaller {

    private Map<String, Constructor> localName2Constructor = new HashMap<>();
    private Map<String, String> attributeName2PropertyName = new HashMap<>();

    /**
     * @see #newInstance(Class)
     */
    private JaxbUnmarshaller() {
    }

    public Object unmarshal(Element element) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String localName = element.getLocalName();
        Object instance = newInstanceForLocalName(localName);
        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(instance);

        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (isNamespaceDeclaration(attr)) {
                continue;
            }

            String propertyName = resolvePropertyName(attr.getName());
            bean.setPropertyValue(propertyName, attr.getValue());
        }

        return instance;
    }

    private boolean isNamespaceDeclaration(Attr attr) {
        String fullName = attr.getName();

        return fullName.equals("xmlns") || fullName.startsWith("xmlns:");
    }

    private Object newInstanceForLocalName(String localName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor constructor = localName2Constructor.get(localName);
        boolean originalAccessibility = constructor.isAccessible();
        try {
            constructor.setAccessible(true);
            return constructor.newInstance();
        } finally {
            constructor.setAccessible(originalAccessibility);
        }
    }

    private String resolvePropertyName(String attributeName) {
        String propertyName = attributeName2PropertyName.get(attributeName);

        return propertyName != null ? propertyName : attributeName;
    }

    public static JaxbUnmarshaller newInstance(Class<?> type) throws NoSuchMethodException {
        JaxbUnmarshaller unmarshaller = new JaxbUnmarshaller();

        String localName = Introspector.decapitalize(type.getSimpleName());
        Constructor constructor = type.getDeclaredConstructor();
        unmarshaller.localName2Constructor.put(localName, constructor);

        Class<?> jaxbType = type;
        while (jaxbType != Object.class) {
            XmlAccessorType xmlAccessorType = jaxbType.getAnnotation(XmlAccessorType.class);
            switch (xmlAccessorType.value()) {
                case FIELD:
                    for (Field field : jaxbType.getDeclaredFields()) {
                        XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);
                        String attributeName = xmlAttribute.name();
                        if (!attributeName.equals("##default")) {
                            unmarshaller.attributeName2PropertyName.put(attributeName, field.getName());
                        }
                    }
                    break;
                case PROPERTY:
                    for (Method method : jaxbType.getDeclaredMethods()) {
                        XmlAttribute xmlAttribute = method.getAnnotation(XmlAttribute.class);
                        if (xmlAttribute == null) {
                            continue;
                        }
                        String attributeName = xmlAttribute.name();
                        if (!attributeName.equals("##default")) {
                            unmarshaller.attributeName2PropertyName.put(attributeName, getPropertyName(method));
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("XML Access Type not supported yet: " + xmlAccessorType.value());
            }

            jaxbType = jaxbType.getSuperclass();
        }

        return unmarshaller;
    }

    private static String getPropertyName(Method method) {
        String propertyName = method.getName();
        if (propertyName.startsWith("is")) {
            propertyName = propertyName.substring(2);
        } else {
            // Assume is setXXX/getXXX
            propertyName = propertyName.substring(3);
        }

        return Introspector.decapitalize(propertyName);
    }
}
