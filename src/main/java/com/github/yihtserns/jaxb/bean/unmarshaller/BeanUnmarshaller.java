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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
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
class BeanUnmarshaller implements Unmarshaller.InitializableUnmarshaller {

    public static final String AUTO_GENERATED_NAME = "##default";
    Set<String> listTypeElementNames = new HashSet<String>();
    Map<String, String> elementName2PropertyName = new HashMap<String, String>();
    Map<String, String> attributeName2PropertyName = new HashMap<String, String>();
    Map<String, XmlAdapter> attributeName2Adapter = new HashMap<String, XmlAdapter>();
    Map<String, Unmarshaller> localName2Unmarshaller = new HashMap<String, Unmarshaller>();
    String textContentPropertyName = null;
    final Class<?> beanClass;
    Constructor constructor;

    BeanUnmarshaller(Constructor constructor) {
        this.beanClass = constructor.getDeclaringClass();
        this.constructor = constructor;
    }

    @Override
    public void init(Provider unmarshallerProvider) throws Exception {
        Class<?> currentClass = beanClass;
        while (currentClass != Object.class) {
            XmlAccessorType xmlAccessorType = currentClass.getAnnotation(XmlAccessorType.class);
            PropertyResolver resolver = getResolverFor(xmlAccessorType);
            for (AccessibleObject accObj : resolver.getDirectMembers(currentClass)) {
                if (accObj.isAnnotationPresent(XmlAttribute.class)) {
                    addAttribute(accObj, resolver);
                } else if (accObj.isAnnotationPresent(XmlElement.class)) {
                    XmlElement[] xmlElements = {accObj.getAnnotation(XmlElement.class)};
                    addElements(xmlElements, accObj, resolver, unmarshallerProvider);
                } else if (accObj.isAnnotationPresent(XmlElements.class)) {
                    XmlElements xmlElements = accObj.getAnnotation(XmlElements.class);
                    addElements(xmlElements.value(), accObj, resolver, unmarshallerProvider);
                } else if (accObj.isAnnotationPresent(XmlElementRef.class)) {
                    addElementRef(accObj, resolver, unmarshallerProvider);
                } else if (accObj.isAnnotationPresent(XmlValue.class)) {
                    setTextContent(accObj, resolver);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    public <T extends AccessibleObject> void addAttribute(T accObj, PropertyResolver<T> resolver) throws Exception {
        XmlAttribute xmlAttribute = accObj.getAnnotation(XmlAttribute.class);

        String propertyName = resolver.getPropertyName(accObj);
        String attributeName = returnNameOrDefault(xmlAttribute.name(), propertyName);

        if (accObj.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
            XmlAdapter adapter = accObj.getAnnotation(XmlJavaTypeAdapter.class).value().newInstance();
            attributeName2Adapter.put(attributeName, adapter);
        }
        attributeName2PropertyName.put(attributeName, propertyName);
    }

    public <T extends AccessibleObject> void addElements(
            XmlElement[] xmlElements,
            T accObj,
            PropertyResolver<T> resolver,
            Unmarshaller.Provider unmarshallerProvider) throws Exception {
        final String propertyName = resolver.getPropertyName(accObj);
        XmlElementWrapper elementWrapper = accObj.getAnnotation(XmlElementWrapper.class);

        if (elementWrapper != null) {
            String wrapperElementName = returnNameOrDefault(elementWrapper.name(), propertyName);

            WrapperUnmarshaller wrapperUnmarshaller = new WrapperUnmarshaller();
            for (XmlElement xmlElement : xmlElements) {
                Unmarshaller childUnmarshaller = resolveUnmarshaller(resolver, accObj, xmlElement, unmarshallerProvider);

                String elementName = returnNameOrDefault(xmlElement.name(), propertyName);
                wrapperUnmarshaller.put(elementName, childUnmarshaller);
            }

            elementName2PropertyName.put(wrapperElementName, propertyName);
            localName2Unmarshaller.put(wrapperElementName, wrapperUnmarshaller);
        } else {
            for (XmlElement xmlElement : xmlElements) {
                String elementName = returnNameOrDefault(xmlElement.name(), propertyName);
                Unmarshaller childUnmarshaller = resolveUnmarshaller(resolver, accObj, xmlElement, unmarshallerProvider);

                if (resolver.isListType(accObj)) {
                    listTypeElementNames.add(elementName);
                }
                elementName2PropertyName.put(elementName, propertyName);
                localName2Unmarshaller.put(elementName, childUnmarshaller);
            }
        }
    }

    private String returnNameOrDefault(String name, String autogeneratedName) {
        return !name.equals(AUTO_GENERATED_NAME) ? name : autogeneratedName;
    }

    private <T extends AccessibleObject> Unmarshaller resolveUnmarshaller(
            PropertyResolver<T> resolver,
            T accObj,
            XmlElement xmlElement,
            Unmarshaller.Provider unmarshallerProvider) throws Exception {

        if (accObj.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
            Class<? extends XmlAdapter> adapterClass = accObj.getAnnotation(XmlJavaTypeAdapter.class).value();

            Class<?> valueType = (Class) ((ParameterizedType) adapterClass.getGenericSuperclass()).getActualTypeArguments()[0];
            Unmarshaller unmarshaller = unmarshallerProvider.getUnmarshallerForType(valueType);

            XmlAdapter adapter = adapterClass.newInstance();
            return new XmlAdapterUnmarshaller(adapter, unmarshaller);
        }

        Class<?> type = xmlElement.type();
        if (type == XmlElement.DEFAULT.class) {
            type = resolver.getComponentType(accObj);
        }

        return unmarshallerProvider.getUnmarshallerForType(type);
    }

    public <T extends AccessibleObject> void addElementRef(
            final T accObj,
            final PropertyResolver<T> resolver,
            Unmarshaller.Provider unmarshallerProvider) {
        Class<?> propertyType = resolver.getComponentType(accObj);
        final String propertyName = resolver.getPropertyName(accObj);

        unmarshallerProvider.forGlobalUnmarshallerCompatibleWith(propertyType, new Provider.Handler() {
            public void handle(String globalName, Unmarshaller unmarshaller) {
                elementName2PropertyName.put(globalName, propertyName);
                localName2Unmarshaller.put(globalName, unmarshaller);
                if (resolver.isListType(accObj)) {
                    listTypeElementNames.add(globalName);
                }
            }
        });
    }

    private <T extends AccessibleObject> void setTextContent(T accObj, PropertyResolver<T> resolver) {
        this.textContentPropertyName = resolver.getPropertyName(accObj);
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
            bean.setPropertyValue(propertyName, childInstance);
        }
        if (textContentPropertyName != null) {
            bean.setPropertyValue(textContentPropertyName, element.getTextContent());
        }
        return instance;
    }

    private boolean isNamespaceDeclaration(Attr attr) {
        String fullName = attr.getName();
        return fullName.equals("xmlns") || fullName.startsWith("xmlns:");
    }

}
