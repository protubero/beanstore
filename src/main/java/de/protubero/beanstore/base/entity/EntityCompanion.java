package de.protubero.beanstore.base.entity;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public final class EntityCompanion<T extends AbstractEntity> extends AbstractCompanion<T> {

	public static final Logger log = LoggerFactory.getLogger(EntityCompanion.class);
	
	private Class<T> beanClass;
	private BeanInfo beanInfo;
	private List<PropertyDescriptor> descriptors;
	private Map<String, PropertyDescriptor> descriptorMap;
	private String alias;
	private Class<T> originalBeanClass;
	
	@SuppressWarnings("unchecked")
	public EntityCompanion(Class<T> originalBeanClass) {
		this.originalBeanClass = originalBeanClass;
		Entity entityAnnotytion = originalBeanClass.getAnnotation(Entity.class);
		alias = entityAnnotytion.alias();
		
		this.beanClass = (Class<T>) new ByteBuddy().subclass(originalBeanClass)
	            .method(ElementMatchers.isSetter())
	            .intercept(MethodDelegation.to(new GenericInterceptor()))
	            .make()
	            .load(originalBeanClass.getClassLoader())
	            .getLoaded();		
		
		try {
			beanInfo = Introspector.getBeanInfo(beanClass, AbstractEntity.class);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}        
        descriptors = Arrays.asList(beanInfo.getPropertyDescriptors()).stream().filter(desc -> {
        	if (desc.getPropertyType().getName().startsWith("groovy.lang.MetaClass")) {
        		log.info("Omit groovy metaClass property");
        		return false;
        	}        	
        	return true;
        }).collect(Collectors.toList());
        log.info("Number properties of entity " + alias + ":" + descriptors.size());
        
        descriptorMap = new HashMap<>();
		descriptors.forEach(desc -> {
			log.info("bean property " + desc.getName());
			
			if (desc.getReadMethod() == null) {
				throw new RuntimeException("Property read method not found: " + alias + '.' + desc.getName());
			}
			if (desc.getWriteMethod() == null) {
				throw new RuntimeException("Property write method not found: " + alias + '.' + desc.getName());
			}
			
			descriptorMap.put(desc.getName(), desc);
		});
		
	}
	

	public Class<T> beanClass() {
		return beanClass;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public T createInstance() {
		T newInstance;
		try {
			newInstance = beanClass().newInstance();
			newInstance.companion(this);
			return newInstance;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

		
	@Override
	public String alias() {
		return alias;
	}

	public List<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}

	public Set<String> propertySet() {
		return descriptors.stream().map(d -> d.getName()).collect(Collectors.toSet());
	}

	public boolean hasProperty(String property) {
		return descriptorMap.containsKey(property);
	}


	@Override
	public Class<T> entityClass() {
		return originalBeanClass;
	}


	public void transferProperties(Map<String, Object> map, T instance) {
		map.forEach((key, value) -> {
			if (value != null) {
				PropertyDescriptor desc = descriptorMap.get(key);
				if (desc != null) {
					instance.put(key, value);
				} else {
					System.out.println("discard n/a " + key + "/" + value);
				}
			} else {
				System.out.println("discard null " + key);
			}	
		});
	}


	@Override
	public Map<String, Object> extractProperties(T instance) {
		Map<String, Object> result = new HashMap<>();
		for (var desc : descriptors) {
			try {
				Object value = desc.getReadMethod().invoke(instance);
				result.put(desc.getName(), value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}


	@Override
	public boolean isMapCompanion() {
		return false;
	}



}
