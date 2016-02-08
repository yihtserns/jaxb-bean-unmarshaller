/*
 * Copyright 2016 yihtserns.
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
package com.github.yihtserns.jaxbean.unmarshaller.api;

import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableCollectionMetadata;
import org.apache.aries.blueprint.mutable.MutablePassThroughMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;

/**
 *
 * @author yihtserns
 */
public class BlueprintBeanHandler implements BeanHandler<MutableBeanMetadata> {

    private ParserContext parserContext;

    public BlueprintBeanHandler(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    @Override
    public MutableBeanMetadata createBean(Class<?> beanClass) throws Exception {
        MutableBeanMetadata beanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setRuntimeClass(beanClass);

        return beanMetadata;
    }

    @Override
    public void setBeanProperty(MutableBeanMetadata bean, String propertyName, Object propertyValue) {
        bean.addProperty(propertyName, toMetadata(propertyValue));
    }

    private Metadata toMetadata(Object value) {
        if (value instanceof Metadata) {
            return (Metadata) value;
        }

        MutableValueMetadata valueMetadata = parserContext.createMetadata(MutableValueMetadata.class);
        valueMetadata.setStringValue((String) value);

        return valueMetadata;
    }

    @Override
    public Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
        MutableCollectionMetadata argumentsMetadata = parserContext.createMetadata(MutableCollectionMetadata.class);
        argumentsMetadata.addValue(toMetadata(from));

        MutablePassThroughMetadata xmlAdapterMetadata = parserContext.createMetadata(MutablePassThroughMetadata.class);
        xmlAdapterMetadata.setObject(xmlAdapter);

        MutableBeanMetadata factoryBeanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        factoryBeanMetadata.setRuntimeClass(MethodInvokingFactoryBean.class);
        factoryBeanMetadata.setInitMethod("afterPropertiesSet");
        factoryBeanMetadata.addProperty("targetObject", xmlAdapterMetadata);
        factoryBeanMetadata.addProperty("targetMethod", toMetadata("unmarshal"));
        factoryBeanMetadata.addProperty("arguments", argumentsMetadata);

        MutableBeanMetadata beanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setFactoryComponent(factoryBeanMetadata);
        beanMetadata.setFactoryMethod("getObject");

        return beanMetadata;
    }

    @Override
    public Object postProcessList(List<Object> unprocessedList) {
        MutableCollectionMetadata processedCollection = parserContext.createMetadata(MutableCollectionMetadata.class);
        for (Object value : unprocessedList) {
            processedCollection.addValue(toMetadata(value));
        }

        return processedCollection;
    }

    @Override
    public Object postProcess(MutableBeanMetadata bean) {
        return bean;
    }
}
