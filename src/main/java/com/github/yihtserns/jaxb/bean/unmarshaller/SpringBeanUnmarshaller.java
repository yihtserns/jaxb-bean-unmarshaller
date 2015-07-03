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

import java.util.List;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author yihtserns
 */
public class SpringBeanUnmarshaller extends BeanUnmarshaller {

    private Class<?> beanClass;

    public SpringBeanUnmarshaller(Class<?> beanClass) throws Exception {
        super(beanClass);
        this.beanClass = beanClass;
    }

    @Override
    protected WrapperUnmarshaller newWrapperUnmarshaller() {
        return new SpringWrapperUnmarshaller();
    }

    @Override
    public BeanDefinition unmarshal(Element element) throws Exception {
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (isNamespaceDeclaration(attr)) {
                continue;
            }
            String attributeName = attr.getName();

            String propertyName = attributeName2PropertyName.get(attributeName);
            bean.addPropertyValue(propertyName, attr.getValue());
        }
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element childElement = (Element) item;
            String localName = item.getLocalName();

            ElementUnmarshaller childUnmarshaller = localName2Unmarshaller.get(localName);
            Object childInstance = childUnmarshaller.unmarshal(childElement);
            String propertyName = elementName2PropertyName.get(localName);

            if (listTypeElementNames.contains(localName)) {
                PropertyValue propertyValue = bean.getRawBeanDefinition().getPropertyValues().getPropertyValue(propertyName);
                List valueList;
                if (propertyValue == null) {
                    valueList = new ManagedList();
                } else {
                    valueList = (List) propertyValue.getValue();
                }
                ((List) valueList).add(childInstance);
                childInstance = valueList;
            }


            bean.addPropertyValue(propertyName, childInstance);
        }

        if (textContentPropertyName != null) {
            bean.addPropertyValue(textContentPropertyName, element.getTextContent());
        }

        return bean.getBeanDefinition();
    }

    private boolean isNamespaceDeclaration(Attr attr) {
        String fullName = attr.getName();
        return fullName.equals("xmlns") || fullName.startsWith("xmlns:");
    }
}
