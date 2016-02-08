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
package com.github.yihtserns.jaxbean.unmarshaller.api;

import com.github.yihtserns.jaxbean.unmarshaller.AbstractSpecTest.JaxbObject;
import com.github.yihtserns.jaxbean.unmarshaller.AbstractSpecTest.JaxbObject2;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yihtserns
 */
@XmlRootElement(namespace = "http://example.com/jaxb")
@XmlAccessorType(value = XmlAccessType.FIELD)
public final class Root {

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
