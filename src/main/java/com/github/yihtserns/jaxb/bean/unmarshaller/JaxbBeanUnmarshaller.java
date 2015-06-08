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

import com.github.yihtserns.jaxb.bean.unmarshaller.Unmarshaller.InitializableUnmarshaller;
import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    private Map<Class<?>, InitializableUnmarshaller> type2Unmarshaller
            = new HashMap<Class<?>, InitializableUnmarshaller>();
    private Map<Class<?>, InitializableUnmarshaller> type2InitializedUnmarshaller
            = new HashMap<Class<?>, InitializableUnmarshaller>();

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
        Unmarshaller unmarshaller = getUnmarshallerForType(type);

        globalName2Unmarshaller.put(elementName, unmarshaller);
        globalType2Name.put(type, elementName);
    }

    private void init() throws Exception {
        while (!type2Unmarshaller.isEmpty()) {
            Collection<InitializableUnmarshaller> toBeInitialized = new ArrayList(type2Unmarshaller.values());
            type2InitializedUnmarshaller.putAll(type2Unmarshaller);
            type2Unmarshaller.clear();

            for (InitializableUnmarshaller unmarshaller : toBeInitialized) {
                unmarshaller.init();
            }
        }
    }

    private Unmarshaller getUnmarshallerForType(Class<?> type) throws NoSuchMethodException {
        if (type2InitializedUnmarshaller.containsKey(type)) {
            return type2InitializedUnmarshaller.get(type);
        }
        if (type2Unmarshaller.containsKey(type)) {
            return type2Unmarshaller.get(type);
        }
        if (type == String.class) {
            return StringUnmarshaller.INSTANCE;
        }

        InitializableUnmarshaller unmarshaller = new BeanUnmarshaller(type.getDeclaredConstructor());
        type2Unmarshaller.put(type, unmarshaller);

        return unmarshaller;
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

    private class BeanUnmarshaller implements InitializableUnmarshaller {

        Set<String> listTypeElementNames = new HashSet<String>();
        Map<String, String> elementName2PropertyName = new HashMap<String, String>();
        Map<String, String> attributeName2PropertyName = new HashMap<String, String>();
        Map<String, XmlAdapter> attributeName2Adapter = new HashMap<String, XmlAdapter>();
        Map<String, Unmarshaller> localName2Unmarshaller = new HashMap<String, Unmarshaller>();
        Map<String, XmlAdapter> localName2Adapter = new HashMap<String, XmlAdapter>();
        String textContentPropertyName = null;
        final Class<?> beanClass;
        Constructor constructor;

        private BeanUnmarshaller(Constructor constructor) {
            this.beanClass = constructor.getDeclaringClass();
            this.constructor = constructor;
        }

        @Override
        public Object unmarshal(Element element) throws Exception {
            Object instance = constructor.newInstance();
            BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(instance);
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attr = (Attr) attributes.item(i);
                if (isNamespaceDeclaration(attr)) {
                    continue;
                }
                String attributeName = attr.getName();

                String propertyName = attributeName2PropertyName.get(attributeName);
                Object value = attr.getValue();

                XmlAdapter adapter = attributeName2Adapter.get(attributeName);
                if (adapter != null) {
                    value = adapter.unmarshal(value);
                }

                bean.setPropertyValue(propertyName, value);
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
                String propertyName = elementName2PropertyName.get(localName);

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

                XmlAdapter adapter = localName2Adapter.get(localName);
                if (adapter != null) {
                    childInstance = adapter.unmarshal(childInstance);
                }

                bean.setPropertyValue(propertyName, childInstance);
            }
            if (textContentPropertyName != null) {
                bean.setPropertyValue(textContentPropertyName, element.getTextContent());
            }
            return instance;
        }

        public <T extends AccessibleObject> void addAttribute(T accObj, PropertyResolver<T> resolver) throws Exception {
            String propertyName = resolver.getPropertyName(accObj);

            String attributeName = accObj.getAnnotation(XmlAttribute.class).name();
            if (attributeName.equals(AUTO_GENERATED_NAME)) {
                attributeName = propertyName;
            }

            if (accObj.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
                XmlJavaTypeAdapter xmlJavaTypeAdapter = accObj.getAnnotation(XmlJavaTypeAdapter.class);
                Class<? extends XmlAdapter> adapterClass = xmlJavaTypeAdapter.value();
                XmlAdapter adapter = adapterClass.newInstance();

                attributeName2Adapter.put(attributeName, adapter);
            }

            attributeName2PropertyName.put(attributeName, propertyName);
        }

        public <T extends AccessibleObject> void addElement(XmlElement xmlElement, T accObj, PropertyResolver<T> resolver) throws Exception {
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
            if (accObj.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
                XmlJavaTypeAdapter xmlJavaTypeAdapter = accObj.getAnnotation(XmlJavaTypeAdapter.class);
                Class<? extends XmlAdapter> adapterClass = xmlJavaTypeAdapter.value();
                XmlAdapter adapter = adapterClass.newInstance();
                localName2Adapter.put(elementName, adapter);

                type = (Class) ((ParameterizedType) adapterClass.getGenericSuperclass()).getActualTypeArguments()[0];
            } else {
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

            elementName2PropertyName.put(elementName, propertyName);
            localName2Unmarshaller.put(elementName, childUnmarshaller);
        }

        public <T extends AccessibleObject> void addElementRef(T accObj, PropertyResolver<T> resolver) {
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

        private <T extends AccessibleObject> void setTextContent(T accObj, PropertyResolver<T> resolver) {
            this.textContentPropertyName = resolver.getPropertyName(accObj);
        }

        @Override
        public void init() throws Exception {
            Class<?> currentClass = beanClass;

            while (currentClass != Object.class) {
                XmlAccessorType xmlAccessorType = currentClass.getAnnotation(XmlAccessorType.class);
                PropertyResolver resolver = getResolverFor(xmlAccessorType);

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

        private PropertyResolver getResolverFor(XmlAccessorType xmlAccessorType) throws UnsupportedOperationException {
            switch (xmlAccessorType.value()) {
                case FIELD:
                    return PropertyResolver.FIELD;
                case PROPERTY:
                    return PropertyResolver.METHOD;
                default:
                    throw new UnsupportedOperationException("XML Access Type not supported yet: " + xmlAccessorType.value());
            }
        }

        private boolean isNamespaceDeclaration(Attr attr) {
            String fullName = attr.getName();
            return fullName.equals("xmlns") || fullName.startsWith("xmlns:");
        }
    }
}
