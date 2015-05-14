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
package com.github.yihtserns.jaxb.bean.unmarshaller;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author yihtserns
 */
public class JaxbBeanUnmarshaller {

    private static final String AUTO_GENERATED_NAME = "##default";
    private Map<String, BeanUnmarshaller> globalName2Unmarshaller = new HashMap<String, BeanUnmarshaller>();

    /**
     * @see #newInstance(java.lang.Class...)
     */
    private JaxbBeanUnmarshaller() {
    }

    public Object unmarshal(Element element) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String globalName = element.getLocalName();
        BeanUnmarshaller unmarshaller = globalName2Unmarshaller.get(globalName);

        return unmarshaller.unmarshal(element);
    }

    public static JaxbBeanUnmarshaller newInstance(Class<?>... types) throws NoSuchMethodException {
        JaxbBeanUnmarshaller jaxbBeanUnmarshaller = new JaxbBeanUnmarshaller();
        for (Class<?> type : types) {
            String elementName = resolveRootElementName(type);
            BeanUnmarshaller unmarshaller = jaxbBeanUnmarshaller.newInstance(type);

            jaxbBeanUnmarshaller.globalName2Unmarshaller.put(elementName, unmarshaller);
        }

        return jaxbBeanUnmarshaller;
    }

    private BeanUnmarshaller newInstance(Class<?> type) throws NoSuchMethodException {
        BeanUnmarshaller unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());

        Class<?> jaxbType = type;
        while (jaxbType != Object.class) {
            XmlAccessorType xmlAccessorType = jaxbType.getAnnotation(XmlAccessorType.class);
            switch (xmlAccessorType.value()) {
                case FIELD:
                    for (Field field : jaxbType.getDeclaredFields()) {
                        if (field.isAnnotationPresent(XmlAttribute.class)) {
                            XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);

                            String attributeName = xmlAttribute.name();
                            if (!attributeName.equals(AUTO_GENERATED_NAME)) {
                                unmarshaller.attributeName2PropertyName.put(attributeName, field.getName());
                            }
                        } else if (field.isAnnotationPresent(XmlElement.class)) {
                            BeanUnmarshaller childUnmarshaller = newInstance(field.getType());

                            XmlElement xmlElement = field.getAnnotation(XmlElement.class);
                            String propertyName = field.getName();
                            String elementName = xmlElement.name();
                            if (elementName.equals(AUTO_GENERATED_NAME)) {
                                elementName = field.getName();
                            } else {
                                unmarshaller.elementName2PropertyName.put(elementName, propertyName);
                            }

                            unmarshaller.localName2Unmarshaller.put(elementName, childUnmarshaller);
                        } else if (field.isAnnotationPresent(XmlElementRef.class)) {
                            String globalName = resolveRootElementName(field.getType());
                            unmarshaller.elementName2PropertyName.put(globalName, field.getName());
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

    private static String resolveRootElementName(Class type) {
        return Introspector.decapitalize(type.getSimpleName());
    }

    private class BeanUnmarshaller {

        Map<String, String> elementName2PropertyName = new HashMap<String, String>();
        Map<String, String> attributeName2PropertyName = new HashMap<String, String>();
        Map<String, BeanUnmarshaller> localName2Unmarshaller = new HashMap<String, BeanUnmarshaller>();
        Constructor constructor;

        private BeanUnmarshaller(Constructor constructor) {
            this.constructor = constructor;
        }

        public Object unmarshal(Element element) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Object instance = newInstance();
            BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(instance);
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attr = (Attr) attributes.item(i);
                if (isNamespaceDeclaration(attr)) {
                    continue;
                }
                String propertyName = resolvePropertyName(attr);
                bean.setPropertyValue(propertyName, attr.getValue());
            }
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element childElement = (Element) item;
                String localName = item.getLocalName();

                BeanUnmarshaller childUnmarshaller = globalName2Unmarshaller.get(localName);
                if (childUnmarshaller == null) {
                    childUnmarshaller = localName2Unmarshaller.get(localName);
                }

                Object childInstance = childUnmarshaller.unmarshal(childElement);

                String propertyName = resolvePropertyName(childElement);
                bean.setPropertyValue(propertyName, childInstance);
            }
            return instance;
        }

        private boolean isNamespaceDeclaration(Attr attr) {
            String fullName = attr.getName();
            return fullName.equals("xmlns") || fullName.startsWith("xmlns:");
        }

        private Object newInstance() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            boolean originalAccessibility = constructor.isAccessible();
            try {
                constructor.setAccessible(true);
                return constructor.newInstance();
            } finally {
                constructor.setAccessible(originalAccessibility);
            }
        }

        private String resolvePropertyName(Attr attribute) {
            String attributeName = attribute.getName();
            String propertyName = attributeName2PropertyName.get(attributeName);

            return propertyName != null ? propertyName : attributeName;
        }

        private String resolvePropertyName(Element element) {
            String elementName = element.getLocalName();
            String propertyName = elementName2PropertyName.get(elementName);

            return propertyName != null ? propertyName : elementName;
        }
    }
}
