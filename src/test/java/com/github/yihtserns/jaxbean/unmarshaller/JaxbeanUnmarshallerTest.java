/*
 * Copyright 2015 The Apache Software Foundation.
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

import com.github.yihtserns.jaxbean.unmarshaller.api.InstanceBeanHandler;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author yihtserns
 */
public class JaxbeanUnmarshallerTest extends AbstractSpecTest {

    @Override
    protected <T> T unmarshal(String xml, Class<T> rootType, Class<?>... allTypes) throws Exception {
        JaxbeanUnmarshaller unmarshaller = JaxbeanUnmarshaller.newInstance(merge(rootType, allTypes));

        return rootType.cast(unmarshaller.unmarshal(toElement(xml), InstanceBeanHandler.INSTANCE));
    }

    private static Element toElement(String xml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        Document doc = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement();
    }
}
