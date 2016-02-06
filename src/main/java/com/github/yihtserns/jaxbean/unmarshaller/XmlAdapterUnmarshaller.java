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
package com.github.yihtserns.jaxbean.unmarshaller;

import com.github.yihtserns.jaxbean.unmarshaller.api.BeanHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.w3c.dom.Node;

/**
 *
 * @author yihtserns
 */
class XmlAdapterUnmarshaller<N extends Node> implements Unmarshaller<N> {

    private XmlAdapter xmlAdapter;
    private Unmarshaller<N> delegate;

    public XmlAdapterUnmarshaller(XmlAdapter xmlAdapter, Unmarshaller<N> delegate) {
        this.xmlAdapter = xmlAdapter;
        this.delegate = delegate;
    }

    public Object unmarshal(N node, BeanHandler beanHandler) throws Exception {
        Object value = delegate.unmarshal(node, beanHandler);

        return beanHandler.unmarshalWith(xmlAdapter, value);
    }
}
