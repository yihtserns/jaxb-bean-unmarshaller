JAXBean Unmarshaller
====================
This is Just Another Xml-Bean Unmarshaller.  Created this because I thought it'll be nice if JAXB Unmarshaller can produce things other than JAXB classes' objects.

**Note**: This is a work-in-progress so not everything JAXB-ish is supported.  To see what is supported, see src/test/java/com/github/yihtserns/jaxbean/unmarshaller/AbstractSpecTest.java.

Usage example
------------
```
import com.github.yihtserns.jaxbean.unmarshaller.JaxbeanUnmarshaller;
import org.w3c.dom.Element;
...
Class<?>[] jaxbClasses = { ... };
JaxbeanUnmarshaller unmarshaller = JaxbeanUnmarshaller.newInstance(jaxbClasses);
...
Element element = ...
Object root = unmarshaller.unmarshal(element);
```

Spring Framework support
------------------------
```
import com.github.yihtserns.jaxbean.unmarshaller.JaxbeanUnmarshaller;
import com.github.yihtserns.jaxbean.unmarshaller.api.SpringBeanHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.w3c.dom.Element;
...
Class<?>[] jaxbClasses = { ... };
JaxbeanUnmarshaller unmarshaller = JaxbeanUnmarshaller.newInstance(jaxbClasses);
...
Element element = ...
BeanDefinition rootDef = (BeanDefinition) unmarshaller.unmarshal(element, SpringBeanHandler.INSTANCE);
```

OSGi Blueprint support
----------------------
```
import com.github.yihtserns.jaxbean.unmarshaller.JaxbeanUnmarshaller;
import com.github.yihtserns.jaxbean.unmarshaller.api.BlueprintBeanHandler;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.w3c.dom.Element;
...
Class<?>[] jaxbClasses = { ... };
JaxbeanUnmarshaller unmarshaller = JaxbeanUnmarshaller.newInstance(jaxbClasses);
...
Element element = ...
MutableBeanMetadata rootMetadata = (MutableBeanMetadata) unmarshaller.unmarshal(element, BlueprintBeanHandler.INSTANCE);
```
