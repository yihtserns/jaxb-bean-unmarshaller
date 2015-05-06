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
package org.apache.camel.spring2;

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
public class JaxbUnmarshallerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void canUnmarshalSingleObject() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\"/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void canUnmarshalFieldAttribute() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " count=\"3\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getCount(), is(3));
    }

    @Test
    public void canUnmarshalFieldAttributeWithCustomName() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " displayName=\"JAXB\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getId(), is("JAXB"));
    }

    @Test
    public void canUnmarshalParentAttribute() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " duration=\"100\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getLength(), is(100L));
    }

    @Test
    public void canUnmarshalSetterAttribute() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " valid=\"true\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttribute() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " description=\"JAXB Object\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getDescription(), is("JAXB Object"));
    }

    @Test
    public void canUnmarshalSetterAttributeWithCustomName() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " skip=\"true\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isIgnore(), is(true));
    }

    @Test
    public void canUnmarshalGetterAttributeWithCustomName() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\""
                + " executable=\"false\""
                + "/>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.isRunnable(), is(false));
    }

    @Test
    public void canUnmarshalSingleElement() throws Exception {
        JaxbUnmarshaller unmarshaller = JaxbUnmarshaller.newInstance(JaxbObject.class);

        String xml = "<jaxbObject xmlns=\"http://example.com/jaxb\">\n"
                + "  <child/>\n"
                + "</jaxbObject>";
        JaxbObject result = (JaxbObject) unmarshaller.unmarshal(toElement(xml));

        assertThat(result.getChild(), is(notNullValue()));
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

    private static final class JaxbChild {

    }
}
