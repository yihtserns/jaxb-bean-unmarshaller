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
package com.github.yihtserns.jaxbean.unmarshaller;

import com.github.yihtserns.jaxbean.unmarshaller.api.BeanHandler;
import com.github.yihtserns.jaxbean.unmarshaller.Unmarshaller.InitializableElementUnmarshaller;
import com.github.yihtserns.jaxbean.unmarshaller.Unmarshaller.ElementUnmarshallerProvider;
import com.github.yihtserns.jaxbean.unmarshaller.Unmarshaller.ElementUnmarshallerProvider.Handler;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.w3c.dom.Element;

/**
 *
 * @author yihtserns
 */
public class JaxbeanUnmarshaller {

    private Map<String, Unmarshaller<Element>> globalName2Unmarshaller;

    /**
     * @see #newInstance(java.lang.Class...)
     */
    private JaxbeanUnmarshaller(Map<String, Unmarshaller<Element>> globalName2Unmarshaller) {
        this.globalName2Unmarshaller = globalName2Unmarshaller;
    }

    public Object unmarshal(Element element) throws Exception {
        return unmarshal(element, ObjectBeanHandler.INSTANCE);
    }

    public Object unmarshal(Element element, BeanHandler beanHandler) throws Exception {
        String globalName = element.getLocalName();
        Unmarshaller<Element> unmarshaller = globalName2Unmarshaller.get(globalName);

        return unmarshaller.unmarshal(element, beanHandler);
    }

    public static JaxbeanUnmarshaller newInstance(Class<?>... types) throws Exception {
        Builder builder = new Builder();
        for (Class<?> type : types) {
            builder.addGlobalType(type);
        }
        builder.init();

        return new JaxbeanUnmarshaller(builder.globalName2Unmarshaller);
    }

    private static class Builder implements ElementUnmarshallerProvider {

        private Map<String, Unmarshaller<Element>> globalName2Unmarshaller = new HashMap<String, Unmarshaller<Element>>();
        private Map<Class<?>, String> globalType2Name = new HashMap<Class<?>, String>();
        private Map<Class<?>, InitializableElementUnmarshaller> type2Unmarshaller
                = new HashMap<Class<?>, InitializableElementUnmarshaller>();
        private Map<Class<?>, InitializableElementUnmarshaller> type2InitializedUnmarshaller
                = new HashMap<Class<?>, InitializableElementUnmarshaller>();

        public void init() throws Exception {
            while (!type2Unmarshaller.isEmpty()) {
                Collection<InitializableElementUnmarshaller> toBeInitialized = new ArrayList(type2Unmarshaller.values());
                type2InitializedUnmarshaller.putAll(type2Unmarshaller);
                type2Unmarshaller.clear();

                for (InitializableElementUnmarshaller unmarshaller : toBeInitialized) {
                    unmarshaller.init(this);
                }
            }
        }

        public void addGlobalType(Class<?> type) throws Exception {
            String elementName = resolveRootElementName(type);
            Unmarshaller<Element> unmarshaller = getUnmarshallerForType(type);

            globalName2Unmarshaller.put(elementName, unmarshaller);
            globalType2Name.put(type, elementName);
        }

        @Override
        public Unmarshaller<Element> getUnmarshallerForType(Class<?> type) throws Exception {
            if (type2InitializedUnmarshaller.containsKey(type)) {
                return type2InitializedUnmarshaller.get(type);
            }
            if (type2Unmarshaller.containsKey(type)) {
                return type2Unmarshaller.get(type);
            }
            if (type == String.class) {
                return ElementTextContentUnmarshaller.INSTANCE;
            }

            InitializableElementUnmarshaller unmarshaller = createBeanUnmarshaller(type);
            type2Unmarshaller.put(type, unmarshaller);

            return unmarshaller;
        }

        protected InitializableElementUnmarshaller createBeanUnmarshaller(Class<?> type) throws Exception {
            return new BeanUnmarshaller(type);
        }

        @Override
        public void forGlobalUnmarshallerCompatibleWith(Class<?> type, Handler handler) {
            for (Entry<Class<?>, String> entry : globalType2Name.entrySet()) {
                Class<?> globalType = entry.getKey();
                String globalName = entry.getValue();

                if (!type.isAssignableFrom(globalType)) {
                    continue;
                }
                Unmarshaller<Element> unmarshaller = globalName2Unmarshaller.get(globalName);
                handler.handle(globalName, unmarshaller);
            }
        }

        private String resolveRootElementName(Class<?> type) {
            XmlRootElement xmlRootElement = type.getAnnotation(XmlRootElement.class);
            String name = xmlRootElement.name();
            if (name.equals(BeanUnmarshaller.AUTO_GENERATED_NAME)) {
                name = Introspector.decapitalize(type.getSimpleName());
            }
            return name;
        }
    }

    private enum ObjectBeanHandler implements BeanHandler<BeanWrapper> {

        INSTANCE;

        @Override
        public BeanWrapper createBean(Class<?> beanClass) throws Exception {
            Object instance = beanClass.newInstance();

            return PropertyAccessorFactory.forBeanPropertyAccess(instance);
        }

        @Override
        public void setBeanProperty(BeanWrapper bean, String propertyName, Object propertyValue) {
            bean.setPropertyValue(propertyName, propertyValue);
        }

        @Override
        public List<Object> getOrCreateValueList(BeanWrapper bean, String propertyName) {
            Object valueList = bean.getPropertyValue(propertyName);
            if (valueList == null) {
                valueList = new ArrayList<Object>();
            } else if (valueList.getClass().isArray()) {
                valueList = new ArrayList<Object>(Arrays.asList((Object[]) valueList));
            }

            return (List) valueList;
        }

        @Override
        public Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
            return xmlAdapter.unmarshal(from);
        }

        @Override
        public Object postProcessList(List<Object> unprocessedList) {
            return unprocessedList;
        }

        @Override
        public Object postProcess(BeanWrapper bean) {
            return bean.getWrappedInstance();
        }
    }
}
