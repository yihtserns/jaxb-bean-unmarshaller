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

import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author yihtserns
 */
public interface BeanHandler<T> {

    T createBean(Class<?> beanClass) throws Exception;

    void setBeanProperty(T bean, String propertyName, Object propertyValue);

    List<Object> getOrCreateValueList(T bean, String propertyName);

    List<Object> newList();

    Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception;

    Object postProcess(T bean);
}
