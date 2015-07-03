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
package com.github.yihtserns.jaxb.bean.unmarshaller;

import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.Annotation;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.JaxbChild;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.JaxbChild2;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.JaxbObject;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.JaxbObject2;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.JaxbParent;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.Message;
import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshallerTest.Note;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author yihtserns
 */
public class SpringJaxbBeanUnmarshallerTest {

    @Test
    public void canUnmarshalSingleObject() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\"/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void canUnmarshalFieldAttribute() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " count=\"3\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("count", is("3")));
        assertThat(result.getCount(), is(3));
    }

    @Test
    public void canUnmarshalFieldAttributeWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " displayName=\"JAXB\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("id", is("JAXB")));
        assertThat(result.getId(), is("JAXB"));
    }

    @Test
    public void canUnmarshalParentAttribute() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " duration=\"100\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("length", is("100")));
        assertThat(result.getLength(), is(100L));
    }

    @Test
    public void canUnmarshalSetterAttribute() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " valid=\"true\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("valid", is("true")));
        assertThat(result.isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttribute() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " description=\"JAXB Object\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("description", is("JAXB Object")));
        assertThat(result.getDescription(), is("JAXB Object"));
    }

    @Test
    public void canUnmarshalSetterAttributeWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " skip=\"true\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("ignore", is("true")));
        assertThat(result.isIgnore(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttributeWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " executable=\"false\""
                + "/>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("runnable", is("false")));
        assertThat(result.isRunnable(), is(false));
    }

    @Test
    public void canUnmarshalChildElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child/>\n"
                + "</jaxbObject>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("child", is(instanceOf(BeanDefinition.class))));
        assertThat(result.getChild(), is(notNullValue()));
    }

    @Test
    public void canUnmarshalChildElementWithFieldAttribute() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child name=\"A Child\"/>\n"
                + "</jaxbObject>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);
        JaxbChild child = result.getChild();

        assertThat(bd, propertyValue("child", propertyValue("name", is("A Child"))));
        assertThat(child.getName(), is("A Child"));
    }

    @Test
    public void canUnmarshalChildElementWithFieldAttributeWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child counter=\"100\"/>\n"
                + "</jaxbObject>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);
        JaxbChild child = result.getChild();

        assertThat(bd, propertyValue("child", propertyValue("count", is("100"))));
        assertThat(child.getCount(), is(100L));
    }

    @Test
    public void canUnmarshalChildElementWithParentAttributes() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child"
                + "    duration=\"200\""
                + "    valid=\"true\""
                + "    description=\"A Child Element\""
                + "    skip=\"false\""
                + "    executable=\"true\""
                + "  />\n"
                + "</jaxbObject>";
        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);
        JaxbChild child = result.getChild();

        assertThat(bd, propertyValue("child", propertyValue("length", is("200"))));
        assertThat(bd, propertyValue("child", propertyValue("valid", is("true"))));
        assertThat(bd, propertyValue("child", propertyValue("description", is("A Child Element"))));
        assertThat(bd, propertyValue("child", propertyValue("ignore", is("false"))));
        assertThat(bd, propertyValue("child", propertyValue("runnable", is("true"))));
        assertThat(child.getLength(), is(200L));
        assertThat(child.isValid(), is(true));
        assertThat(child.getDescription(), is("A Child Element"));
        assertThat(child.isIgnore(), is(false));
        assertThat(child.isRunnable(), is(true));
    }

    @Test
    public void canUnmarshalChildRootElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class, JaxbObject2.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <secondJaxbObject valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);
        JaxbObject2 globalChild = result.getGlobalChild();

        assertThat(bd, propertyValue("globalChild", propertyValue("valid", is("true"))));
        assertThat(globalChild.isValid(), is(true));
    }

    @Test
    public void canUnmarshalNamedChildElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <childWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("namedChild", propertyValue("valid", is("true"))));
        assertThat(result.getNamedChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalParentChildElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <parentChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("parentChild", propertyValue("valid", is("true"))));
        assertThat(result.getParentChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalParentChildElementWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <parentChildWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("namedParentChild", propertyValue("valid", is("true"))));
        assertThat(result.getNamedParentChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalSetterElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <setterChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("setterChild", propertyValue("valid", is("true"))));
        assertThat(result.getSetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <getterChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("getterChild", propertyValue("valid", is("true"))));
        assertThat(result.getGetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterElementWithCustomName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <setterChildWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(bd, propertyValue("namedSetterChild", propertyValue("valid", is("true"))));
        assertThat(result.getNamedSetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalArrayChildElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <childrenArray valid=\"true\"/>\n"
                + "  <childrenArray valid=\"false\"/>\n"
                + "  <childrenArray valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getChildrenArray(), arrayWithSize(3));
        assertThat(result.getChildrenArray()[0].isValid(), is(true));
        assertThat(result.getChildrenArray()[1].isValid(), is(false));
        assertThat(result.getChildrenArray()[2].isValid(), is(true));
    }

    @Test
    public void canUnmarshalListChildElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <children valid=\"true\"/>\n"
                + "  <children valid=\"false\"/>\n"
                + "  <children valid=\"true\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getChildren(), hasSize(3));
        assertThat(result.getChildren().get(0).isValid(), is(true));
        assertThat(result.getChildren().get(1).isValid(), is(false));
        assertThat(result.getChildren().get(2).isValid(), is(true));
    }

    @Test
    public void canUnmarshalStringList() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <alias>This</alias>\n"
                + "  <alias>That</alias>\n"
                + "  <alias>It</alias>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getAliases(), contains("This", "That", "It"));
    }

    @Test
    public void canUnmarshalElementWrapper() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <options1>\n"
                + "    <options1>skip-invalid</options1>\n"
                + "    <options1>purge-skipped</options1>\n"
                + "  </options1>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getOptions1(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomElementName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <options2>\n"
                + "    <option2>skip-invalid</option2>\n"
                + "    <option2>purge-skipped</option2>\n"
                + "  </options2>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getOptions2(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomWrapperName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions3>\n"
                + "    <options3>skip-invalid</options3>\n"
                + "    <options3>purge-skipped</options3>\n"
                + "  </wrappedOptions3>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getOptions3(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomWrapperAndElementName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions4>\n"
                + "    <option4>skip-invalid</option4>\n"
                + "    <option4>purge-skipped</option4>\n"
                + "  </wrappedOptions4>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getOptions4(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalNonStringElementWrapperWithCustomWrapperAndElementName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions5>\n"
                + "    <option5 valid=\"true\"/>\n"
                + "    <option5 valid=\"false\"/>\n"
                + "  </wrappedOptions5>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getOptions5(), hasSize(2));
        assertThat(result.getOptions5().get(0).isValid(), is(true));
        assertThat(result.getOptions5().get(1).isValid(), is(false));
    }

    @Test
    public void canUnmarshalTypedElement() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "    <typedChild name=\"A Child\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getTypedChild(), (Matcher) isA(JaxbChild.class));
        assertThat(((JaxbChild) result.getTypedChild()).getName(), is("A Child"));
    }

    @Test
    public void canUnmarshalXmlElements() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        {
            String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <child1 valid=\"true\"/>\n"
                    + "</jaxbObject>";

            BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
            JaxbObject result = asBean(bd);

            assertThat(result.getTwoTypes(), (Matcher) isA(JaxbChild.class));
            assertThat(result.getTwoTypes().isValid(), is(true));
        }

        {
            String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <child2 valid=\"false\"/>\n"
                    + "</jaxbObject>";

            BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
            JaxbObject result = asBean(bd);

            assertThat(result.getTwoTypes(), (Matcher) isA(JaxbChild2.class));
            assertThat(result.getTwoTypes().isValid(), is(false));
        }
    }

    @Test
    public void canUnmarshalXmlElementsList() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <childList1 valid=\"true\"/>\n"
                + "  <childList2 valid=\"false\"/>\n"
                + "  <childList2 valid=\"true\"/>\n"
                + "  <childList1 valid=\"false\"/>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        List<JaxbParent> list = result.getTwoTypeList();
        {
            JaxbParent child = list.get(0);
            assertThat(child, (Matcher) isA(JaxbChild.class));
            assertThat(child.isValid(), is(true));
        }
        {
            JaxbParent child = list.get(1);
            assertThat(child, (Matcher) isA(JaxbChild2.class));
            assertThat(child.isValid(), is(false));
        }
        {
            JaxbParent child = list.get(2);
            assertThat(child, (Matcher) isA(JaxbChild2.class));
            assertThat(child.isValid(), is(true));
        }
        {
            JaxbParent child = list.get(3);
            assertThat(child, (Matcher) isA(JaxbChild.class));
            assertThat(child.isValid(), is(false));
        }
    }

    @Test
    public void canUnmarshalElementsWrapper() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <options6>\n"
                + "    <child1 valid=\"true\"/>\n"
                + "    <child2 valid=\"false\"/>\n"
                + "  </options6>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        List<JaxbParent> list = result.getOptions6();
        {
            JaxbParent child = list.get(0);
            assertThat(child, (Matcher) isA(JaxbChild.class));
            assertThat(child.isValid(), is(true));
        }
        {
            JaxbParent child = list.get(1);
            assertThat(child, (Matcher) isA(JaxbChild2.class));
            assertThat(child.isValid(), is(false));
        }
    }

    @Test
    public void canUnmarshalXmlValue() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child>\n"
                + "    <note>A child</note>\n"
                + "  </child>\n"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        assertThat(result.getChild().getNote().getText(), is("A child"));
    }

    @Test
    public void canUnmarshalXmlElementRefSublasses() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class, JaxbObject2.class);

        {
            String xml = "<secondJaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <jaxbObject displayName=\"First ref\"/>\n"
                    + "</secondJaxbObject>";

            BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
            JaxbObject2 result = asBean(bd);

            assertThat(((JaxbObject) result.getMultiGlobalChild()).getId(), is("First ref"));
        }
        {
            String xml = "<secondJaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <secondJaxbObject valid=\"true\"/>\n"
                    + "</secondJaxbObject>";

            BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
            JaxbObject2 result = asBean(bd);

            assertThat(((JaxbObject2) result.getMultiGlobalChild()).isValid(), is(true));
        }
    }

    @Test
    public void canUnmarshalXmlElementRefList() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject.class, Message.class, Annotation.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <message>1st rev: An object</message>\n"
                + "  <annotation>Dangerous one</annotation>"
                + "  <message>2nd rev: Reduce power</message>"
                + "</jaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject result = asBean(bd);

        List<Note<?>> notes = result.getNotes();
        assertThat(((Message) notes.get(0)).getText(), is("1st rev: An object"));
        assertThat(((Annotation) notes.get(1)).getText(), is("Dangerous one"));
        assertThat(((Message) notes.get(2)).getText(), is("2nd rev: Reduce power"));
    }

    @Test
    public void shouldNotConfuseLocalElementForGlobalElementWithSameName() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject2.class, Annotation.class);

        String xml = "<secondJaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <annotation>WIP</annotation>"
                + "</secondJaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject2 result = asBean(bd);

        assertThat(result.getAnnotation().getText(), is("WIP"));
    }

    @Test
    public void canUnmarshalElementWithXmlJavaTypeAdapter() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject2.class);

        String xml = "<secondJaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <metadata>\n"
                + "    <entry key=\"author\" value=\"Me\"/>\n"
                + "    <entry key=\"obsolete\" value=\"Yes\"/>\n"
                + "  </metadata>\n"
                + "</secondJaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject2 result = asBean(bd);

        Map<String, String> metadata = result.getMetadata();
        assertThat(metadata, hasEntry("author", "Me"));
        assertThat(metadata, hasEntry("obsolete", "Yes"));
    }

    @Test
    public void canUnmarshalAttributeWithXmlJavaTypeAdapter() throws Exception {
        SpringJaxbBeanUnmarshaller unmarshaller = SpringJaxbBeanUnmarshaller.newInstance(JaxbObject2.class);

        String xml = "<secondJaxbObject"
                + "      xmlns=\"http://example.com/jaxb\""
                + "      annotationAttr=\"WIP\">\n"
                + "</secondJaxbObject>";

        BeanDefinition bd = (BeanDefinition) unmarshaller.unmarshal(toElement(xml));
        JaxbObject2 result = asBean(bd);

        assertThat(result.getAnnotationAttr().getText(), is("WIP"));
    }

    private static Element toElement(String xml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        Document doc = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement();
    }

    private static <T> Matcher<BeanDefinition> propertyValue(final String propertyName, Matcher<T> propertyValueMatcher) {
        String description = "property '" + propertyName + "'";
        return new FeatureMatcher<BeanDefinition, T>(propertyValueMatcher, description, description) {

            @Override
            protected T featureValueOf(BeanDefinition bd) {
                return (T) bd.getPropertyValues().get(propertyName);
            }
        };
    }

    private static <T> T asBean(BeanDefinition bd) {
        final String beanName = "bean";

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition(beanName, bd);

        return (T) beanFactory.getBean(beanName);
    }
}
