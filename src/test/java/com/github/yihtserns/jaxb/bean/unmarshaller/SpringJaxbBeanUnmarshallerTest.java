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
package com.github.yihtserns.jaxb.bean.unmarshaller;

import java.io.StringReader;
import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * @author yihtserns
 */
public class SpringJaxbBeanUnmarshallerTest extends AbstractSpecTest {

    @Override
    protected <T> T unmarshal(String xml, Class<T> rootType, Class<?>... allTypes) throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(merge(rootType, allTypes));
        final UnmarshallerNamespaceHandler unmarshallerNamespaceHandler = new UnmarshallerNamespaceHandler(unmarshaller);

        GenericApplicationContext appContext = new GenericApplicationContext();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext) {

            @Override
            protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
                final NamespaceHandlerResolver defaultResolver = super.createDefaultNamespaceHandlerResolver();
                return new NamespaceHandlerResolver() {

                    public NamespaceHandler resolve(String namespaceUri) {
                        if (namespaceUri.equals("http://example.com/jaxb")) {
                            return unmarshallerNamespaceHandler;
                        }
                        return defaultResolver.resolve(namespaceUri);
                    }
                };
            }
        };
        xmlReader.setValidating(false);
        xmlReader.loadBeanDefinitions(new InputSource(new StringReader(xml)));
        appContext.refresh();

        return appContext.getBean(rootType);
    }

    private static final class UnmarshallerNamespaceHandler extends AbstractBeanDefinitionParser implements NamespaceHandler {

        private SpringJaxbBeanUnmarshaller unmarshaller;

        public UnmarshallerNamespaceHandler(SpringJaxbBeanUnmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        @Override
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            try {
                return (AbstractBeanDefinition) unmarshaller.unmarshal(element);
            } catch (Exception ex) {
                String localName = parserContext.getDelegate().getLocalName(element);

                parserContext.getReaderContext().fatal("Unable to unmarshal element '" + localName + "'", element, ex);
                return null;
            }
        }

        @Override
        protected boolean shouldGenerateIdAsFallback() {
            return true;
        }

        public void init() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
