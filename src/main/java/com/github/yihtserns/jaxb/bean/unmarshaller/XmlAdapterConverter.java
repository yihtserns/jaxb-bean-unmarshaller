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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Node;

/**
 *
 * @author yihtserns
 */
public class XmlAdapterConverter {

    public static Object convert(XmlAdapter xmlAdapter, Object value) throws Exception {
        return xmlAdapter.unmarshal(value);
    }

    static class XmlAdapterUnmarshaller implements Unmarshaller {

        private Class objectType;
        private XmlAdapter xmlAdapter;
        private com.github.yihtserns.jaxb.bean.unmarshaller.Unmarshaller delegate;

        public XmlAdapterUnmarshaller(Class objectType, XmlAdapter xmlAdapter, Unmarshaller delegate) {
            this.objectType = objectType;
            this.xmlAdapter = xmlAdapter;
            this.delegate = delegate;
        }

        public Object unmarshal(Node node) throws Exception {
            Object instance = delegate.unmarshal(node);

            return BeanDefinitionBuilder.genericBeanDefinition(XmlAdapterConverter.class)
                    .setFactoryMethod("convert")
                    .addConstructorArgValue(xmlAdapter)
                    .addConstructorArgValue(instance)
                    .getBeanDefinition();
        }

        public static <N extends Node> Unmarshaller<N> create(XmlAdapter xmlAdapter, Unmarshaller<N> delegate) {
            Class<? extends XmlAdapter> adapterClass = xmlAdapter.getClass();
            Type boundType = ((ParameterizedType) adapterClass.getGenericSuperclass()).getActualTypeArguments()[1];

            Class objectType = boundType instanceof Class ? (Class) boundType : (Class) ((ParameterizedType) boundType).getRawType();

            return new XmlAdapterUnmarshaller(objectType, xmlAdapter, delegate);
        }
    }
}
