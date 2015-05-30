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

import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.Matcher;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author yihtserns
 */
public class JaxbBeanUnmarshallerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void canUnmarshalSingleObject() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\"/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void canUnmarshalFieldAttribute() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " count=\"3\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getCount(), is(3));
    }

    @Test
    public void canUnmarshalFieldAttributeWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " displayName=\"JAXB\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getId(), is("JAXB"));
    }

    @Test
    public void canUnmarshalParentAttribute() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " duration=\"100\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getLength(), is(100L));
    }

    @Test
    public void canUnmarshalSetterAttribute() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " valid=\"true\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttribute() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " description=\"JAXB Object\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getDescription(), is("JAXB Object"));
    }

    @Test
    public void canUnmarshalSetterAttributeWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " skip=\"true\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isIgnore(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttributeWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " executable=\"false\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isRunnable(), is(false));
    }

    @Test
    public void canUnmarshalChildElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child/>\n"
                + "</jaxbObject>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getChild(), is(notNullValue()));
    }

    @Test
    public void canUnmarshalChildElementWithFieldAttribute() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child name=\"A Child\"/>\n"
                + "</jaxbObject>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        JaxbChild child = result.getChild();
        assertThat(child.getName(), is("A Child"));
    }

    @Test
    public void canUnmarshalChildElementWithFieldAttributeWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child counter=\"100\"/>\n"
                + "</jaxbObject>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        JaxbChild child = result.getChild();
        assertThat(child.getCount(), is(100L));
    }

    @Test
    public void canUnmarshalChildElementWithParentAttributes() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child"
                + "    duration=\"200\""
                + "    valid=\"true\""
                + "    description=\"A Child Element\""
                + "    skip=\"false\""
                + "    executable=\"true\""
                + "  />\n"
                + "</jaxbObject>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        JaxbChild child = result.getChild();
        assertThat(child.getLength(), is(200L));
        assertThat(child.isValid(), is(true));
        assertThat(child.getDescription(), is("A Child Element"));
        assertThat(child.isIgnore(), is(false));
        assertThat(child.isRunnable(), is(true));
    }

    @Test
    public void canUnmarshalChildRootElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class, JaxbObject2.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <jaxbObject2 valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        JaxbObject2 globalChild = result.getGlobalChild();
        assertThat(globalChild.isValid(), is(true));
    }

    @Test
    public void canUnmarshalNamedChildElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <childWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getNamedChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalParentChildElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <parentChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getParentChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalParentChildElementWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <parentChildWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getNamedParentChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalSetterElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <setterChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getSetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <getterChild valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getGetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterElementWithCustomName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <setterChildWithName valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getNamedSetterChild().isValid(), is(true));
    }

    @Test
    public void canUnmarshalListChildElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <children valid=\"true\"/>\n"
                + "  <children valid=\"false\"/>\n"
                + "  <children valid=\"true\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getChildren(), hasSize(3));
        assertThat(result.getChildren().get(0).isValid(), is(true));
        assertThat(result.getChildren().get(1).isValid(), is(false));
        assertThat(result.getChildren().get(2).isValid(), is(true));
    }

    @Test
    public void canUnmarshalStringList() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <alias>This</alias>\n"
                + "  <alias>That</alias>\n"
                + "  <alias>It</alias>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getAliases(), contains("This", "That", "It"));
    }

    @Test
    public void canUnmarshalElementWrapper() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <options1>\n"
                + "    <options1>skip-invalid</options1>\n"
                + "    <options1>purge-skipped</options1>\n"
                + "  </options1>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getOptions1(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomElementName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <options2>\n"
                + "    <option2>skip-invalid</option2>\n"
                + "    <option2>purge-skipped</option2>\n"
                + "  </options2>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getOptions2(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomWrapperName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions3>\n"
                + "    <options3>skip-invalid</options3>\n"
                + "    <options3>purge-skipped</options3>\n"
                + "  </wrappedOptions3>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getOptions3(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalElementWrapperWithCustomWrapperAndElementName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions4>\n"
                + "    <option4>skip-invalid</option4>\n"
                + "    <option4>purge-skipped</option4>\n"
                + "  </wrappedOptions4>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getOptions4(), contains(
                "skip-invalid",
                "purge-skipped"));
    }

    @Test
    public void canUnmarshalNonStringElementWrapperWithCustomWrapperAndElementName() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <wrappedOptions5>\n"
                + "    <option5 valid=\"true\"/>\n"
                + "    <option5 valid=\"false\"/>\n"
                + "  </wrappedOptions5>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getOptions5(), hasSize(2));
        assertThat(result.getOptions5().get(0).isValid(), is(true));
        assertThat(result.getOptions5().get(1).isValid(), is(false));
    }

    @Test
    public void canUnmarshalTypedElement() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "    <typedChild name=\"A Child\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
        assertThat(result.getTypedChild(), (Matcher) isA(JaxbChild.class));
        assertThat(((JaxbChild) result.getTypedChild()).getName(), is("A Child"));
    }

    @Test
    public void canUnmarshalXmlElements() throws Exception {
        JaxbBeanUnmarshaller unmarshaller = JaxbBeanUnmarshaller.newInstance(JaxbObject.class);

        {
            String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <child1 valid=\"true\"/>\n"
                    + "</jaxbObject>";

            JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
            assertThat(result.getTwoTypes(), (Matcher) isA(JaxbChild.class));
            assertThat(result.getTwoTypes().isValid(), is(true));
        }

        {
            String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                    + "  <child2 valid=\"false\"/>\n"
                    + "</jaxbObject>";

            JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
            assertThat(result.getTwoTypes(), (Matcher) isA(JaxbChild2.class));
            assertThat(result.getTwoTypes().isValid(), is(false));
        }
    }

    @Test
    public void canUnmarshalXmlElementsList() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(JaxbObject.class).createUnmarshaller();

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <childList1 valid=\"true\"/>\n"
                + "  <childList2 valid=\"false\"/>\n"
                + "  <childList2 valid=\"true\"/>\n"
                + "  <childList1 valid=\"false\"/>\n"
                + "</jaxbObject>";

        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));
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

    private static Element toElement(String xml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        Document doc = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement();
    }

    @XmlRootElement(namespace = "http://example.com/jaxb")
    private static final class JaxbObject extends JaxbParent {

        @XmlAttribute(name = "displayName")
        private String id;
        @XmlAttribute
        private Integer count;
        @XmlElement
        private JaxbChild child;
        @XmlElementRef
        private JaxbObject2 globalChild;
        @XmlElement(name = "childWithName")
        private JaxbChild namedChild;
        @XmlElement(type = JaxbChild.class)
        private JaxbParent typedChild;
        @XmlElements({
            @XmlElement(name = "child1", type = JaxbChild.class),
            @XmlElement(name = "child2", type = JaxbChild2.class)
        })
        private JaxbParent twoTypes;
        @XmlElements({
            @XmlElement(name = "childList1", type = JaxbChild.class),
            @XmlElement(name = "childList2", type = JaxbChild2.class)
        })
        private List<JaxbParent> twoTypeList;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public JaxbChild getChild() {
            return child;
        }

        public void setChild(JaxbChild child) {
            this.child = child;
        }

        public JaxbObject2 getGlobalChild() {
            return globalChild;
        }

        public void setGlobalChild(JaxbObject2 globalChild) {
            this.globalChild = globalChild;
        }

        public JaxbChild getNamedChild() {
            return namedChild;
        }

        public void setNamedChild(JaxbChild namedChild) {
            this.namedChild = namedChild;
        }

        public JaxbParent getTypedChild() {
            return typedChild;
        }

        public void setTypedChild(JaxbParent typedChild) {
            this.typedChild = typedChild;
        }

        public JaxbParent getTwoTypes() {
            return twoTypes;
        }

        public void setTwoTypes(JaxbParent twoTypes) {
            this.twoTypes = twoTypes;
        }

        public List<JaxbParent> getTwoTypeList() {
            return twoTypeList;
        }

        public void setTwoTypeList(List<JaxbParent> twoTypeList) {
            this.twoTypeList = twoTypeList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class JaxbParent extends JaxbParent2 {

        @XmlAttribute(name = "duration")
        private Long length;
        @XmlElement
        private JaxbChild parentChild;
        @XmlElement(name = "parentChildWithName")
        private JaxbChild namedParentChild;
        @XmlElement
        private List<JaxbChild> children;
        @XmlElement(name = "alias")
        private List<String> aliases;
        @XmlElementWrapper
        @XmlElement
        private List<String> options1;
        @XmlElementWrapper
        @XmlElement(name = "option2")
        private List<String> options2;
        @XmlElementWrapper(name = "wrappedOptions3")
        @XmlElement
        private List<String> options3;
        @XmlElementWrapper(name = "wrappedOptions4")
        @XmlElement(name = "option4")
        private List<String> options4;
        @XmlElementWrapper(name = "wrappedOptions5")
        @XmlElement(name = "option5")
        private List<JaxbChild> options5;

        public Long getLength() {
            return length;
        }

        public void setLength(Long length) {
            this.length = length;
        }

        public JaxbChild getParentChild() {
            return parentChild;
        }

        public void setParentChild(JaxbChild parentChild) {
            this.parentChild = parentChild;
        }

        public JaxbChild getNamedParentChild() {
            return namedParentChild;
        }

        public void setNamedParentChild(JaxbChild namedParentChild) {
            this.namedParentChild = namedParentChild;
        }

        public List<JaxbChild> getChildren() {
            return children;
        }

        public void setChildren(List<JaxbChild> children) {
            this.children = children;
        }

        public List<String> getAliases() {
            return aliases;
        }

        public void setAliases(List<String> aliases) {
            this.aliases = aliases;
        }

        public List<String> getOptions1() {
            return options1;
        }

        public void setOptions1(List<String> options1) {
            this.options1 = options1;
        }

        public List<String> getOptions2() {
            return options2;
        }

        public void setOptions2(List<String> options2) {
            this.options2 = options2;
        }

        public List<String> getOptions3() {
            return options3;
        }

        public void setOptions3(List<String> options3) {
            this.options3 = options3;
        }

        public List<String> getOptions4() {
            return options4;
        }

        public void setOptions4(List<String> options4) {
            this.options4 = options4;
        }

        public List<JaxbChild> getOptions5() {
            return options5;
        }

        public void setOptions5(List<JaxbChild> options5) {
            this.options5 = options5;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class JaxbParent2 {

        private Boolean validity;
        private String comment;
        private Boolean ignore;
        private Boolean runnable;
        private JaxbChild setterChild;
        private JaxbChild getterChild;
        private JaxbChild namedSetterChild;

        @XmlAttribute
        public Boolean isValid() {
            return validity;
        }

        public void setValid(Boolean valid) {
            this.validity = valid;
        }

        public String getDescription() {
            return comment;
        }

        @XmlAttribute
        public void setDescription(String comment) {
            this.comment = comment;
        }

        public Boolean isIgnore() {
            return ignore;
        }

        @XmlAttribute(name = "skip")
        public void setIgnore(Boolean ignore) {
            this.ignore = ignore;
        }

        @XmlAttribute(name = "executable")
        public Boolean isRunnable() {
            return runnable;
        }

        public void setRunnable(Boolean runnable) {
            this.runnable = runnable;
        }

        public JaxbChild getSetterChild() {
            return setterChild;
        }

        @XmlElement
        public void setSetterChild(JaxbChild setterChild) {
            this.setterChild = setterChild;
        }

        @XmlElement
        public JaxbChild getGetterChild() {
            return getterChild;
        }

        public void setGetterChild(JaxbChild getterChild) {
            this.getterChild = getterChild;
        }

        public JaxbChild getNamedSetterChild() {
            return namedSetterChild;
        }

        @XmlElement(name = "setterChildWithName")
        public void setNamedSetterChild(JaxbChild namedSetterChild) {
            this.namedSetterChild = namedSetterChild;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static final class JaxbChild extends JaxbParent {

        @XmlAttribute
        private String name;
        @XmlAttribute(name = "counter")
        private Long count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    private static final class JaxbChild2 extends JaxbParent {

    }

    @XmlRootElement
    private static final class JaxbObject2 extends JaxbParent {

    }
}
