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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.w3c.dom.Element;
import static org.hamcrest.Matchers.*;
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

    @Test
    public void canResolvePropertyPlaceholders() throws Exception {
        String xml = "<beans xmlns=\"http://www.springframework.org/schema/beans\""
                + "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + "     xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\""
                + "  >\n"
                + "  <bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n"
                + "    <property name=\"properties\">\n"
                + "      <value>\n"
                + "        obj.count=3\n"
                + "        obj.displayName=JAXB\n"
                + "        obj.duration=#{10*10}\n"
                + "        obj.valid=true\n"
                + "        obj.description=${obj.displayName} Object\n"
                + "        obj.skip=true\n"
                + "        obj.executable=false\n"
                + "        obj.child.name=A Child\n"
                + "        obj.child.counter=100\n"
                + "        obj.child.duration=200\n"
                + "        obj.child.valid=true\n"
                + "        obj.child.description=${obj.child.name} Element\n"
                + "        obj.child.skip=false\n"
                + "        obj.child.executable=true\n"
                + "        obj.secondObj.valid=true\n"
                + "        obj.child.named.valid=true\n"
                + "        obj.parent.child.valid=true\n"
                + "        obj.parent.child.named.valid=true\n"
                + "        obj.setter.child.valid=true\n"
                + "        obj.getter.child.valid=true\n"
                + "        obj.setter.child.named.valid=true\n"
                + "        obj.children[1].valid=true\n"
                + "        obj.children[2].valid=false\n"
                + "        obj.children[3].valid=true\n"
                + "        obj.children.1.valid=true\n"
                + "        obj.children.2.valid=false\n"
                + "        obj.children.3.valid=true\n"
                + "        obj.child.note=A child\n"
                + "        obj.alias[1]=This\n"
                + "        obj.alias[2]=That\n"
                + "        obj.alias[3]=It\n"
                + "        obj.options1.options1.1=skip-invalid\n"
                + "        obj.options1.options1.2=purge-skipped\n"
                + "        obj.options2.option2.1=skip-invalid\n"
                + "        obj.options2.option2.2=purge-skipped\n"
                + "        obj.wrappedOptions3.options3.1=skip-invalid\n"
                + "        obj.wrappedOptions3.options3.2=purge-skipped\n"
                + "        obj.wrappedOptions4.option4.1=skip-invalid\n"
                + "        obj.wrappedOptions4.option4.2=purge-skipped\n"
                + "        obj.wrappedOptions5.option5.1.valid=true\n"
                + "        obj.wrappedOptions5.option5.2.valid=false\n"
                + "        obj.child.typed.name=A Child\n"
                + "        obj.child1.valid=true\n"
                + "        obj.childList1.1.valid=true\n"
                + "        obj.childList2.1.valid=false\n"
                + "        obj.childList2.2.valid=true\n"
                + "        obj.childList1.2.valid=false\n"
                + "        obj.options6.child1.valid=true\n"
                + "        obj.options6.child2.valid=false\n"
                + "        obj2.annotation=WIP\n"
                + "        obj2.metadata.entry1.key=author\n"
                + "        obj2.metadata.entry1.value=Me\n"
                + "        obj2.metadata.entry2.key=obsolete\n"
                + "        obj2.metadata.entry2.value=Yes\n"
                + "        obj.message1=1st rev: An object\n"
                + "        obj.annotation=Dangerous one\n"
                + "        obj.message2=2nd rev: Reduce power\n"
                + "        obj2.obj.displayName=First ref\n"
                + "        obj2.annotationAttr=WIP\n"
                + "      </value>\n"
                + "    </property>\n"
                + "  </bean>\n"
                + "\n"
                + "  <root xmlns=\"http://example.com/jaxb\">\n"
                + "    <jaxbObject"
                + "      count=\"${obj.count}\""
                + "      displayName=\"${obj.displayName}\""
                + "      duration=\"${obj.duration}\""
                + "      valid=\"${obj.valid}\""
                + "      description=\"${obj.description}\""
                + "      skip=\"${obj.skip}\""
                + "      executable=\"${obj.executable}\""
                + "    >\n"
                + "      <child"
                + "        name=\"${obj.child.name}\""
                + "        counter=\"${obj.child.counter}\""
                + "        duration=\"${obj.child.duration}\""
                + "        valid=\"${obj.child.valid}\""
                + "        description=\"${obj.child.description}\""
                + "        skip=\"${obj.child.skip}\""
                + "        executable=\"${obj.child.executable}\""
                + "      >\n"
                + "        <note>${obj.child.note}</note>\n"
                + "      </child>\n"
                + "      <secondJaxbObject valid=\"${obj.secondObj.valid}\"/>\n"
                + "      <childWithName valid=\"${obj.child.named.valid}\"/>\n"
                + "      <parentChild valid=\"${obj.parent.child.valid}\"/>\n"
                + "      <parentChildWithName valid=\"${obj.parent.child.named.valid}\"/>\n"
                + "      <setterChild valid=\"${obj.setter.child.valid}\"/>\n"
                + "      <getterChild valid=\"${obj.getter.child.valid}\"/>\n"
                + "      <setterChildWithName valid=\"${obj.setter.child.named.valid}\"/>\n"
                + "      <childrenArray valid=\"${obj.children[1].valid}\"/>\n"
                + "      <childrenArray valid=\"${obj.children[2].valid}\"/>\n"
                + "      <childrenArray valid=\"${obj.children[3].valid}\"/>\n"
                + "      <children valid=\"${obj.children.1.valid}\"/>\n"
                + "      <children valid=\"${obj.children.2.valid}\"/>\n"
                + "      <children valid=\"${obj.children.3.valid}\"/>\n"
                + "      <alias>${obj.alias[1]}</alias>\n"
                + "      <alias>${obj.alias[2]}</alias>\n"
                + "      <alias>${obj.alias[3]}</alias>\n"
                + "      <options1>\n"
                + "        <options1>${obj.options1.options1.1}</options1>\n"
                + "        <options1>${obj.options1.options1.2}</options1>\n"
                + "      </options1>\n"
                + "      <options2>\n"
                + "        <option2>${obj.options2.option2.1}</option2>\n"
                + "        <option2>${obj.options2.option2.2}</option2>\n"
                + "      </options2>\n"
                + "      <wrappedOptions3>\n"
                + "        <options3>${obj.wrappedOptions3.options3.1}</options3>\n"
                + "        <options3>${obj.wrappedOptions3.options3.2}</options3>\n"
                + "      </wrappedOptions3>\n"
                + "      <wrappedOptions4>\n"
                + "        <option4>${obj.wrappedOptions4.option4.1}</option4>\n"
                + "        <option4>${obj.wrappedOptions4.option4.2}</option4>\n"
                + "      </wrappedOptions4>\n"
                + "      <wrappedOptions5>\n"
                + "        <option5 valid=\"${obj.wrappedOptions5.option5.1.valid}\"/>\n"
                + "        <option5 valid=\"${obj.wrappedOptions5.option5.2.valid}\"/>\n"
                + "      </wrappedOptions5>\n"
                + "      <typedChild name=\"${obj.child.typed.name}\"/>\n"
                + "      <child1 valid=\"${obj.child1.valid}\"/>\n"
                + "      <childList1 valid=\"${obj.childList1.1.valid}\"/>\n"
                + "      <childList2 valid=\"${obj.childList2.1.valid}\"/>\n"
                + "      <childList2 valid=\"${obj.childList2.2.valid}\"/>\n"
                + "      <childList1 valid=\"${obj.childList1.2.valid}\"/>\n"
                + "      <options6>\n"
                + "        <child1 valid=\"${obj.options6.child1.valid}\"/>\n"
                + "        <child2 valid=\"${obj.options6.child2.valid}\"/>\n"
                + "      </options6>\n"
                + "      <message>${obj.message1}</message>\n"
                + "      <annotation>${obj.annotation}</annotation>\n"
                + "      <message>${obj.message2}</message>\n"
                + "    </jaxbObject>\n"
                + "    <secondJaxbObject annotationAttr=\"${obj2.annotationAttr}\">\n"
                + "      <jaxbObject displayName=\"${obj2.obj.displayName}\"/>\n"
                + "      <annotation>${obj2.annotation}</annotation>\n"
                + "      <metadata>\n"
                + "        <entry key=\"${obj2.metadata.entry1.key}\" value=\"${obj2.metadata.entry1.value}\"/>\n"
                + "        <entry key=\"${obj2.metadata.entry2.key}\" value=\"${obj2.metadata.entry2.value}\"/>\n"
                + "      </metadata>\n"
                + "    </secondJaxbObject>\n"
                + "  </root>\n"
                + "</beans>";

        Root root = unmarshal(xml, Root.class, JaxbObject.class, JaxbObject2.class, Message.class, Annotation.class);
        {
            JaxbObject result = root.getJaxbObject();
            assertThat(result.getCount(), is(3));
            assertThat(result.getId(), is("JAXB"));
            assertThat(result.getLength(), is(100L));
            assertThat(result.isValid(), is(true));
            assertThat(result.getDescription(), is("JAXB Object"));
            assertThat(result.isIgnore(), is(true));
            assertThat(result.isRunnable(), is(false));
            assertThat(result.getChild().getName(), is("A Child"));
            assertThat(result.getChild().getCount(), is(100L));
            assertThat(result.getChild().getLength(), is(200L));
            assertThat(result.getChild().isValid(), is(true));
            assertThat(result.getChild().getDescription(), is("A Child Element"));
            assertThat(result.getChild().isIgnore(), is(false));
            assertThat(result.getChild().isRunnable(), is(true));
            assertThat(result.getGlobalChild().isValid(), is(true));
            assertThat(result.getNamedChild().isValid(), is(true));
            assertThat(result.getParentChild().isValid(), is(true));
            assertThat(result.getNamedParentChild().isValid(), is(true));
            assertThat(result.getSetterChild().isValid(), is(true));
            assertThat(result.getGetterChild().isValid(), is(true));
            assertThat(result.getNamedSetterChild().isValid(), is(true));
            assertThat(result.getChildrenArray()[0].isValid(), is(true));
            assertThat(result.getChildrenArray()[1].isValid(), is(false));
            assertThat(result.getChildrenArray()[2].isValid(), is(true));
            assertThat(result.getChildren().get(0).isValid(), is(true));
            assertThat(result.getChildren().get(1).isValid(), is(false));
            assertThat(result.getChildren().get(2).isValid(), is(true));
            assertThat(result.getAliases(), contains("This", "That", "It"));
            assertThat(result.getOptions1(), contains("skip-invalid", "purge-skipped"));
            assertThat(result.getOptions2(), contains("skip-invalid", "purge-skipped"));
            assertThat(result.getOptions3(), contains("skip-invalid", "purge-skipped"));
            assertThat(result.getOptions4(), contains("skip-invalid", "purge-skipped"));
            assertThat(result.getOptions5().get(0).isValid(), is(true));
            assertThat(result.getOptions5().get(1).isValid(), is(false));
            assertThat(((JaxbChild) result.getTypedChild()).getName(), is("A Child"));
            assertThat(result.getTwoTypes().isValid(), is(true));
            assertThat(result.getTwoTypeList().get(0).isValid(), is(true));
            assertThat(result.getTwoTypeList().get(1).isValid(), is(false));
            assertThat(result.getTwoTypeList().get(2).isValid(), is(true));
            assertThat(result.getTwoTypeList().get(3).isValid(), is(false));
            assertThat(result.getOptions6().get(0).isValid(), is(true));
            assertThat(result.getOptions6().get(1).isValid(), is(false));
            assertThat(result.getChild().getNote().getText(), is("A child"));
            assertThat(result.getNotes().get(0).getText(), is("1st rev: An object"));
            assertThat(result.getNotes().get(1).getText(), is("Dangerous one"));
            assertThat(result.getNotes().get(2).getText(), is("2nd rev: Reduce power"));
        }
        {
            JaxbObject2 result = root.getSecondJaxbObject();
            assertThat(((JaxbObject) result.getMultiGlobalChild()).getId(), is("First ref"));
            assertThat(result.getAnnotation().getText(), is("WIP"));
            assertThat(result.getMetadata(), hasEntry("author", "Me"));
            assertThat(result.getMetadata(), hasEntry("obsolete", "Yes"));
            assertThat(result.getAnnotationAttr().getText(), is("WIP"));
        }
    }

    @XmlRootElement(namespace = "http://example.com/jaxb")
    @XmlAccessorType(XmlAccessType.FIELD)
    static final class Root {

        @XmlElement
        private JaxbObject jaxbObject;
        @XmlElement
        private JaxbObject2 secondJaxbObject;

        public JaxbObject getJaxbObject() {
            return jaxbObject;
        }

        public void setJaxbObject(JaxbObject jaxbObject) {
            this.jaxbObject = jaxbObject;
        }

        public JaxbObject2 getSecondJaxbObject() {
            return secondJaxbObject;
        }

        public void setSecondJaxbObject(JaxbObject2 secondJaxbObject) {
            this.secondJaxbObject = secondJaxbObject;
        }
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
