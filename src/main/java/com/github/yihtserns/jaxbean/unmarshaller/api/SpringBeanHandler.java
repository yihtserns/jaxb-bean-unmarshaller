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
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;

/**
 *
 * @author yihtserns
 */
public enum SpringBeanHandler implements BeanHandler<BeanDefinitionBuilder> {

    INSTANCE;

    @Override
    public BeanDefinitionBuilder createBean(Class<?> beanClass) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanClass);
    }

    @Override
    public void setBeanProperty(BeanDefinitionBuilder bean, String propertyName, Object propertyValue) {
        bean.addPropertyValue(propertyName, propertyValue);
    }

    @Override
    public List<Object> getOrCreateValueList(BeanDefinitionBuilder bean, String propertyName) {
        PropertyValue propertyValue = bean.getRawBeanDefinition().getPropertyValues().getPropertyValue(propertyName);
        List valueList;
        if (propertyValue == null) {
            valueList = newList();
        } else {
            valueList = (List) propertyValue.getValue();
        }
        return valueList;
    }

    @Override
    public List<Object> newList() {
        return new ManagedList<Object>();
    }

    @Override
    public Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
        return BeanDefinitionBuilder.genericBeanDefinition(MethodInvokingFactoryBean.class)
                .addPropertyValue("targetObject", xmlAdapter)
                .addPropertyValue("targetMethod", "unmarshal")
                .addPropertyValue("arguments", from)
                .getBeanDefinition();
    }

    @Override
    public Object postProcess(BeanDefinitionBuilder bean) {
        return bean.getBeanDefinition();
    }
}
