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
package com.github.yihtserns.jaxb.bean.unmarshaller.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 *
 * @author yihtserns
 */
public enum InstanceBeanHandler implements BeanHandler {

    INSTANCE;

    @Override
    public Object createBean(Class<?> beanClass) throws Exception {
        Object instance = beanClass.newInstance();

        return PropertyAccessorFactory.forBeanPropertyAccess(instance);
    }

    @Override
    public void setBeanProperty(Object bean, String propertyName, Object propertyValue) {
        ((BeanWrapper) bean).setPropertyValue(propertyName, propertyValue);
    }

    @Override
    public List getOrCreateValueList(Object bean, String propertyName) {
        Object valueList = ((BeanWrapper) bean).getPropertyValue(propertyName);
        if (valueList == null) {
            valueList = newList();
        } else if (valueList.getClass().isArray()) {
            valueList = new ArrayList(Arrays.asList((Object[]) valueList));
        }

        return (List) valueList;
    }

    @Override
    public List<Object> newList() {
        return new ArrayList<Object>();
    }

    @Override
    public Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
        return xmlAdapter.unmarshal(from);
    }

    @Override
    public Object postProcess(Object bean) {
        return ((BeanWrapper) bean).getWrappedInstance();
    }
}
