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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    private Map<Class<?>, BeanUnmarshaller> type2Unmarshaller = new HashMap<Class<?>, BeanUnmarshaller>();

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

    private void addGlobalType(Class<?> type) throws NoSuchMethodException {
        String elementName = resolveRootElementName(type);
        BeanUnmarshaller unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());
        init(unmarshaller, type);

        globalName2Unmarshaller.put(elementName, unmarshaller);
    }

    private void init() throws NoSuchMethodException {
        for (Entry<Class<?>, BeanUnmarshaller> entry : type2Unmarshaller.entrySet()) {
            init(entry.getValue(), entry.getKey());
        }
    }

    private BeanUnmarshaller getUnmarshallerForType(Class<?> type) throws NoSuchMethodException {
        BeanUnmarshaller unmarshaller = type2Unmarshaller.get(type);
        if (unmarshaller == null) {
            unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());
            type2Unmarshaller.put(type, unmarshaller);
        }
        return unmarshaller;
    }

    private void init(BeanUnmarshaller unmarshaller, Class<?> type) throws NoSuchMethodException {
        while (type != Object.class) {
            XmlAccessorType xmlAccessorType = type.getAnnotation(XmlAccessorType.class);
            Resolver resolver = getResolverFor(xmlAccessorType);

            for (AccessibleObject accObj : resolver.getDirectMembers(type)) {
                if (accObj.isAnnotationPresent(XmlAttribute.class)) {
                    unmarshaller.addAttribute(accObj, resolver);
                } else if (accObj.isAnnotationPresent(XmlElement.class)) {
                    unmarshaller.addElement(accObj, resolver);
                } else if (accObj.isAnnotationPresent(XmlElementRef.class)) {
                    unmarshaller.addElementRef(accObj, resolver);
                }
            }

            type = type.getSuperclass();
        }
    }

    private Resolver getResolverFor(XmlAccessorType xmlAccessorType) throws UnsupportedOperationException {
        switch (xmlAccessorType.value()) {
            case FIELD:
                return Resolver.FIELD;
            case PROPERTY:
                return Resolver.METHOD;
            default:
                throw new UnsupportedOperationException("XML Access Type not supported yet: " + xmlAccessorType.value());
        }
    }

    public static JaxbBeanUnmarshaller newInstance(Class<?>... types) throws NoSuchMethodException {
        JaxbBeanUnmarshaller jaxbBeanUnmarshaller = new JaxbBeanUnmarshaller();
        for (Class<?> type : types) {
            jaxbBeanUnmarshaller.addGlobalType(type);
        }
        jaxbBeanUnmarshaller.init();

        return jaxbBeanUnmarshaller;
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

        public <T extends AccessibleObject> void addAttribute(T accObj, Resolver<T> resolver) {
            String propertyName = resolver.getPropertyName(accObj);

            String attributeName = accObj.getAnnotation(XmlAttribute.class).name();
            if (!attributeName.equals(AUTO_GENERATED_NAME)) {
                attributeName2PropertyName.put(attributeName, propertyName);
            }
        }

        public <T extends AccessibleObject> void addElement(T accObj, Resolver<T> resolver) throws NoSuchMethodException {
            String propertyName = resolver.getPropertyName(accObj);
            Class<?> type = resolver.getPropertyType(accObj);

            String elementName = accObj.getAnnotation(XmlElement.class).name();
            if (elementName.equals(AUTO_GENERATED_NAME)) {
                elementName = propertyName;
            } else {
                elementName2PropertyName.put(elementName, propertyName);
            }

            BeanUnmarshaller childUnmarshaller = getUnmarshallerForType(type);
            localName2Unmarshaller.put(elementName, childUnmarshaller);
        }

        public <T extends AccessibleObject> void addElementRef(T accObj, Resolver<T> resolver) {
            String globalName = resolveRootElementName(resolver.getPropertyType(accObj));
            elementName2PropertyName.put(globalName, resolver.getPropertyName(accObj));
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

    private static abstract class Resolver<T extends AccessibleObject> {

        public static final Resolver<Method> METHOD = new Resolver<Method>() {

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
                if (method.getName().startsWith("set")) {
                    return method.getParameterTypes()[0];
                }

                // Assume is isXXX/getXXX
                return method.getReturnType();
            }

        };

        private static final Resolver<Field> FIELD = new Resolver<Field>() {

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
        };

        public abstract AccessibleObject[] getDirectMembers(Class<?> type);

        public abstract String getPropertyName(T t);

        public abstract Class<?> getPropertyType(T t);
    }
}