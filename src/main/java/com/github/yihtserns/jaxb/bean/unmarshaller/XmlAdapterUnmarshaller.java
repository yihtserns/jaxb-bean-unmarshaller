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
import org.w3c.dom.Element;

/**
 *
 * @author yihtserns
 */
class XmlAdapterUnmarshaller implements ElementUnmarshaller {

    private XmlAdapter xmlAdapter;
    private ElementUnmarshaller delegate;

    public XmlAdapterUnmarshaller(XmlAdapter xmlAdapter, ElementUnmarshaller delegate) {
        this.xmlAdapter = xmlAdapter;
        this.delegate = delegate;
    }

    public Object unmarshal(Element element) throws Exception {
        return xmlAdapter.unmarshal(delegate.unmarshal(element));
    }
}
