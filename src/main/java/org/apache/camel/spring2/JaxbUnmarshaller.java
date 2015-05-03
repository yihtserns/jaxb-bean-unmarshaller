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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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

            bean.setPropertyValue(attr.getName(), attr.getValue());
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

    public static JaxbUnmarshaller newInstance(Class<?> type) throws NoSuchMethodException {
        JaxbUnmarshaller unmarshaller = new JaxbUnmarshaller();

        String localName = Introspector.decapitalize(type.getSimpleName());
        Constructor constructor = type.getDeclaredConstructor();
        unmarshaller.localName2Constructor.put(localName, constructor);

        return unmarshaller;
    }
}
