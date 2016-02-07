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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableCollectionMetadata;
import org.apache.aries.blueprint.mutable.MutablePassThroughMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.Metadata;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;

/**
 *
 * @author yihtserns
 */
public class BlueprintBeanHandler implements BeanHandler<MutableBeanMetadata> {

    private ParserContext parserContext;

    public BlueprintBeanHandler(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    public MutableBeanMetadata createBean(Class<?> beanClass) throws Exception {
        MutableBeanMetadata beanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setRuntimeClass(beanClass);

        return beanMetadata;
    }

    public void setBeanProperty(MutableBeanMetadata bean, String propertyName, Object propertyValue) {
        bean.addProperty(propertyName, toMetadata(propertyValue));
    }

    public Metadata toMetadata(Object value) {
        if (value instanceof CollectionMetadataList) {
            value = ((CollectionMetadataList) value).collectionMetadata;
        }
        if (value instanceof Metadata) {
            return (Metadata) value;
        }

        MutablePassThroughMetadata valueMetadata = parserContext.createMetadata(MutablePassThroughMetadata.class);
        valueMetadata.setObject(value);

        return valueMetadata;
    }

    public List<Object> getOrCreateValueList(MutableBeanMetadata bean, String propertyName) {
        for (BeanProperty property : bean.getProperties()) {
            if (propertyName.equals(property.getName())) {
                MutableCollectionMetadata valueListMetadata = (MutableCollectionMetadata) property.getValue();
                return new CollectionMetadataList<Object>(valueListMetadata);
            }
        }
        return newList();
    }

    public List<Object> newList() {
        return new CollectionMetadataList<Object>(parserContext.createMetadata(MutableCollectionMetadata.class));
    }

    public Object unmarshalWith(XmlAdapter xmlAdapter, Object from) throws Exception {
        MutableCollectionMetadata argumentsMetadata = parserContext.createMetadata(MutableCollectionMetadata.class);
        argumentsMetadata.addValue(toMetadata(from));

        MutableBeanMetadata factoryBeanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        factoryBeanMetadata.setRuntimeClass(MethodInvokingFactoryBean.class);
        factoryBeanMetadata.setInitMethod("afterPropertiesSet");
        factoryBeanMetadata.addProperty("targetObject", toMetadata(xmlAdapter));
        factoryBeanMetadata.addProperty("targetMethod", toMetadata("unmarshal"));
        factoryBeanMetadata.addProperty("arguments", argumentsMetadata);

        MutableBeanMetadata beanMetadata = parserContext.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setFactoryComponent(factoryBeanMetadata);
        beanMetadata.setFactoryMethod("getObject");

        return beanMetadata;
    }

    public Object postProcess(MutableBeanMetadata bean) {
        return bean;
    }

    private final class CollectionMetadataList<T> implements List<T> {

        private MutableCollectionMetadata collectionMetadata;

        public CollectionMetadataList(MutableCollectionMetadata collectionMetadata) {
            this.collectionMetadata = collectionMetadata;
        }

        public boolean add(T e) {
            collectionMetadata.addValue(toMetadata(e));

            return true;
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public Iterator<T> iterator() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public <T> T[] toArray(T[] ts) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean containsAll(Collection<?> clctn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean addAll(Collection<? extends T> clctn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean addAll(int i, Collection<? extends T> clctn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean removeAll(Collection<?> clctn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean retainAll(Collection<?> clctn) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public T get(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public T set(int i, T e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public void add(int i, T e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public T remove(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public int indexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public ListIterator<T> listIterator() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public ListIterator<T> listIterator(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public List<T> subList(int i, int i1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
