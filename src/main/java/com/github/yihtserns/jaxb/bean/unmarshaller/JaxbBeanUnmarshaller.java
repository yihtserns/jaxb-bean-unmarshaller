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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
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
    private Map<String, Unmarshaller> globalName2Unmarshaller = new HashMap<String, Unmarshaller>();
    private Map<Class<?>, String> globalType2Name = new HashMap<Class<?>, String>();
    private Map<Class<?>, Unmarshaller> type2Unmarshaller = new HashMap<Class<?>, Unmarshaller>();
    private Map<Class<?>, Unmarshaller> type2InitializedUnmarshaller = new HashMap<Class<?>, Unmarshaller>();

    /**
     * @see #newInstance(java.lang.Class...)
     */
    private JaxbBeanUnmarshaller() {
    }

    public Object unmarshal(Element element) throws Exception {
        String globalName = element.getLocalName();
        Unmarshaller unmarshaller = globalName2Unmarshaller.get(globalName);

        return unmarshaller.unmarshal(element);
    }

    private void addGlobalType(Class<?> type) throws Exception {
        String elementName = resolveRootElementName(type);
        BeanUnmarshaller unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());

        globalName2Unmarshaller.put(elementName, unmarshaller);
        globalType2Name.put(type, elementName);
    }

    private void init() throws Exception {
        for (Unmarshaller unmarshaller : globalName2Unmarshaller.values()) {
            unmarshaller.init();
        }

        while (!type2Unmarshaller.isEmpty()) {
            type2InitializedUnmarshaller.putAll(type2Unmarshaller);
            type2Unmarshaller.clear();

            for (Unmarshaller unmarshaller : type2InitializedUnmarshaller.values()) {
                unmarshaller.init();
            }
        }
    }

    private Unmarshaller getUnmarshallerForType(Class<?> type) throws NoSuchMethodException {
        Unmarshaller unmarshaller = type2InitializedUnmarshaller.get(type);
        if (unmarshaller == null) {
            unmarshaller = type2Unmarshaller.get(type);
        }
        if (unmarshaller == null) {
            if (type == String.class) {
                unmarshaller = new StringUnmarshaller();
            } else {
                unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());
            }
            type2Unmarshaller.put(type, unmarshaller);
        }
        return unmarshaller;
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

    public static JaxbBeanUnmarshaller newInstance(Class<?>... types) throws Exception {
        JaxbBeanUnmarshaller jaxbBeanUnmarshaller = new JaxbBeanUnmarshaller();
        for (Class<?> type : types) {
            jaxbBeanUnmarshaller.addGlobalType(type);
        }
        jaxbBeanUnmarshaller.init();

        return jaxbBeanUnmarshaller;
    }

    private static String resolveRootElementName(Class<?> type) {
        XmlRootElement xmlRootElement = type.getAnnotation(XmlRootElement.class);
        String name = xmlRootElement.name();
        if (name.equals(AUTO_GENERATED_NAME)) {
            name = Introspector.decapitalize(type.getSimpleName());
        }
        return name;
    }

    private interface Unmarshaller {

        public Object unmarshal(Element element) throws Exception;

        public void init() throws Exception;
    }

    private class StringUnmarshaller implements Unmarshaller {

        public Object unmarshal(Element element) {
            return element.getTextContent();
        }

        public void init() {
        }
    }

    private class WrapperUnmarshaller implements Unmarshaller {

        private Map<String, Unmarshaller> localName2Unmarshaller = new HashMap<String, Unmarshaller>();

        public Object unmarshal(Element element) throws Exception {
            List<Object> result = new ArrayList<Object>();

            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element childElement = (Element) item;
                String localName = childElement.getLocalName();

                Unmarshaller unmarshaller = localName2Unmarshaller.get(localName);
                if (unmarshaller != null) {
                    Object instance = unmarshaller.unmarshal(childElement);
                    result.add(instance);
                }
            }

            return result;
        }

        public void put(String localName, Unmarshaller unmarshaller) {
            this.localName2Unmarshaller.put(localName, unmarshaller);
        }

        public void init() throws Exception {
        }
    }

    private class BeanUnmarshaller implements Unmarshaller {

        Set<String> listTypeElementNames = new HashSet<String>();
        Map<String, String> elementName2PropertyName = new HashMap<String, String>();
        Map<String, String> attributeName2PropertyName = new HashMap<String, String>();
        Map<String, Unmarshaller> localName2Unmarshaller = new HashMap<String, Unmarshaller>();
        String textContentPropertyName = null;
        final Class<?> beanClass;
        Constructor constructor;

        private BeanUnmarshaller(Constructor constructor) {
            this.beanClass = constructor.getDeclaringClass();
            this.constructor = constructor;
        }

        public Object unmarshal(Element element) throws Exception {
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

                Unmarshaller childUnmarshaller = localName2Unmarshaller.get(localName);
                if (childUnmarshaller == null) {
                    childUnmarshaller = globalName2Unmarshaller.get(localName);
                }

                Object childInstance = childUnmarshaller.unmarshal(childElement);
                String propertyName = resolvePropertyName(childElement);

                if (listTypeElementNames.contains(localName)) {
                    Object valueList = bean.getPropertyValue(propertyName);
                    if (valueList == null) {
                        valueList = new ArrayList();
                    } else if (valueList.getClass().isArray()) {
                        valueList = new ArrayList(Arrays.asList((Object[]) valueList));
                    }

                    ((List) valueList).add(childInstance);
                    childInstance = valueList;
                }
                bean.setPropertyValue(propertyName, childInstance);
            }
            if (textContentPropertyName != null) {
                bean.setPropertyValue(textContentPropertyName, element.getTextContent());
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

        public <T extends AccessibleObject> void addElement(XmlElement xmlElement, T accObj, Resolver<T> resolver) throws NoSuchMethodException {
            String propertyName = resolver.getPropertyName(accObj);

            boolean wrapped = accObj.isAnnotationPresent(XmlElementWrapper.class);
            String elementName;
            if (wrapped) {
                elementName = accObj.getAnnotation(XmlElementWrapper.class).name();
            } else {
                elementName = xmlElement.name();
            }

            if (elementName.equals(AUTO_GENERATED_NAME)) {
                elementName = propertyName;
            }

            Class<?> type = resolver.getPropertyType(accObj);
            if (type == List.class) {
                type = resolver.getListComponentType(accObj);

                if (!wrapped) {
                    listTypeElementNames.add(elementName);
                }
            } else if (type.isArray()) {
                type = type.getComponentType();

                listTypeElementNames.add(elementName);
            }

            Class<?> elementType = xmlElement.type();
            if (elementType != XmlElement.DEFAULT.class) {
                type = elementType;
            }

            Unmarshaller childUnmarshaller = getUnmarshallerForType(type);
            if (wrapped) {
                String wrappedElementName = xmlElement.name();
                if (wrappedElementName.equals(AUTO_GENERATED_NAME)) {
                    wrappedElementName = propertyName;
                }

                WrapperUnmarshaller wrapperUnmarshaller = (WrapperUnmarshaller) localName2Unmarshaller.get(elementName);
                if (wrapperUnmarshaller == null) {
                    wrapperUnmarshaller = new WrapperUnmarshaller();
                }
                wrapperUnmarshaller.put(wrappedElementName, childUnmarshaller);
                childUnmarshaller = wrapperUnmarshaller;
            }

            if (!elementName.equals(propertyName)) {
                elementName2PropertyName.put(elementName, propertyName);
            }
            localName2Unmarshaller.put(elementName, childUnmarshaller);
        }

        public <T extends AccessibleObject> void addElementRef(T accObj, Resolver<T> resolver) {
            Class<?> propertyType = resolver.getPropertyType(accObj);
            String propertyName = resolver.getPropertyName(accObj);

            boolean isListType = (propertyType == List.class);
            if (isListType) {
                propertyType = resolver.getListComponentType(accObj);
            }

            for (Entry<Class<?>, String> entry : globalType2Name.entrySet()) {
                Class<?> globalType = entry.getKey();
                if (propertyType.isAssignableFrom(globalType)) {
                    String globalName = entry.getValue();
                    elementName2PropertyName.put(globalName, propertyName);
                    if (isListType) {
                        listTypeElementNames.add(globalName);
                    }
                }
            }
        }

        private <T extends AccessibleObject> void setTextContent(T accObj, Resolver<T> resolver) {
            this.textContentPropertyName = resolver.getPropertyName(accObj);
        }

        public void init() throws Exception {
            Class<?> currentClass = beanClass;

            while (currentClass != Object.class) {
                XmlAccessorType xmlAccessorType = currentClass.getAnnotation(XmlAccessorType.class);
                Resolver resolver = getResolverFor(xmlAccessorType);

                for (AccessibleObject accObj : resolver.getDirectMembers(currentClass)) {
                    if (accObj.isAnnotationPresent(XmlAttribute.class)) {
                        addAttribute(accObj, resolver);
                    } else if (accObj.isAnnotationPresent(XmlElement.class)) {
                        XmlElement xmlElement = accObj.getAnnotation(XmlElement.class);
                        addElement(xmlElement, accObj, resolver);
                    } else if (accObj.isAnnotationPresent(XmlElements.class)) {
                        XmlElements xmlElements = accObj.getAnnotation(XmlElements.class);
                        for (XmlElement xmlElement : xmlElements.value()) {
                            addElement(xmlElement, accObj, resolver);
                        }
                    } else if (accObj.isAnnotationPresent(XmlElementRef.class)) {
                        addElementRef(accObj, resolver);
                    } else if (accObj.isAnnotationPresent(XmlValue.class)) {
                        setTextContent(accObj, resolver);
                    }
                }

                currentClass = currentClass.getSuperclass();
            }
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
            Type genericType = getGenericType(t);
            return (Class) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
    }
}
