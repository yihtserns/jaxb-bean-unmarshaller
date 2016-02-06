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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Node;

/**
 *
 * @author yihtserns
 */
class SpringXmlAdapterUnmarshaller<N extends Node> extends XmlAdapterUnmarshaller<N> {

    public SpringXmlAdapterUnmarshaller(XmlAdapter xmlAdapter, Unmarshaller delegate) {
        super(xmlAdapter, delegate);
    }

    @Override
    protected Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
        return BeanDefinitionBuilder.genericBeanDefinition(MethodInvokingFactoryBean.class)
                .addPropertyValue("targetObject", xmlAdapter)
                .addPropertyValue("targetMethod", "unmarshal")
                .addPropertyValue("arguments", from)
                .getBeanDefinition();
    }
}
