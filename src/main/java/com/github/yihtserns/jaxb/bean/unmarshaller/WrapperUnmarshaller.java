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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author yihtserns
 */
class WrapperUnmarshaller implements ElementUnmarshaller {

    private Map<String, Unmarshaller<Element>> localName2Unmarshaller = new HashMap<String, Unmarshaller<Element>>();

    @Override
    public Object unmarshal(Element element) throws Exception {
        List<Object> result = newList();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element childElement = (Element) item;
            String localName = childElement.getLocalName();
            Unmarshaller<Element> unmarshaller = localName2Unmarshaller.get(localName);
            if (unmarshaller != null) {
                Object instance = unmarshaller.unmarshal(childElement);
                result.add(instance);
            }
        }
        return result;
    }

    protected List<Object> newList() {
        return new ArrayList<Object>();
    }

    public void put(String localName, Unmarshaller<Element> unmarshaller) {
        this.localName2Unmarshaller.put(localName, unmarshaller);
    }
}
