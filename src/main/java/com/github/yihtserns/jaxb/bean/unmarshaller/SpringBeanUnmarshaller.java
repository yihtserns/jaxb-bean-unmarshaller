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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Node;

/**
 *
 * @author yihtserns
 */
class SpringBeanUnmarshaller extends BeanUnmarshaller {

    public SpringBeanUnmarshaller(Class<?> beanClass) throws Exception {
        super(beanClass);
    }

    @Override
    protected <N extends Node> Unmarshaller<N> newXmlAdapterUnmarshaller(XmlAdapter adapter, Unmarshaller<N> unmarshaller) {
        return new SpringXmlAdapterUnmarshaller(adapter, unmarshaller);
    }

    @Override
    protected ElementWrapperUnmarshaller newWrapperUnmarshaller() {
        return new SpringElementWrapperUnmarshaller();
    }

    @Override
    protected BeanDefinitionBuilder createBean(Class<?> beanClass) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanClass);
    }

    @Override
    protected void setBeanProperty(Object bean, String propertyName, Object propertyValue) {
        ((BeanDefinitionBuilder) bean).addPropertyValue(propertyName, propertyValue);
    }

    @Override
    protected List getOrCreateValueList(Object bean, String propertyName) {
        PropertyValue propertyValue = ((BeanDefinitionBuilder) bean).getRawBeanDefinition().getPropertyValues().getPropertyValue(propertyName);
        List valueList;
        if (propertyValue == null) {
            valueList = new ManagedList();
        } else {
            valueList = (List) propertyValue.getValue();
        }
        return valueList;
    }

    @Override
    protected Object postProcess(Object bean) {
        return ((BeanDefinitionBuilder) bean).getBeanDefinition();
    }
}
