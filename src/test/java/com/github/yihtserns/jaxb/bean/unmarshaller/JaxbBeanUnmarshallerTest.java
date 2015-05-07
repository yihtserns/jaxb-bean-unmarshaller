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

import com.github.yihtserns.jaxb.bean.unmarshaller.JaxbBeanUnmarshaller;
import java.io.StringReader;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class JaxbParent extends JaxbParent2 {

        @XmlAttribute(name = "duration")
        private Long length;

        public Long getLength() {
            return length;
        }

        public void setLength(Long length) {
            this.length = length;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class JaxbParent2 {

        private Boolean validity;
        private String comment;
        private Boolean ignore;
        private Boolean runnable;

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
}
