package de.protubero.beanstore.entity;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private Constructor<T> constructor;

	
	@SuppressWarnings("unchecked")
	public EntityCompanion(Class<T> originalBeanClass) {
		if (!AbstractEntity.class.isAssignableFrom(originalBeanClass)) {
			throw new RuntimeException("No tricks, dude.");
		}
		
		try {
			if (originalBeanClass.getConstructor() == null) {
				throw new RuntimeException("Missing no-arg constructor in entity class " + originalBeanClass);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
		this.originalBeanClass = originalBeanClass;
		Entity entityAnnotytion = originalBeanClass.getAnnotation(Entity.class);
		alias = entityAnnotytion.alias();
		
		this.beanClass = (Class<T>) new ByteBuddy().subclass(originalBeanClass)
				.implement(GeneratedClass.class)
	            .method(ElementMatchers.isSetter())
	            .intercept(MethodDelegation.to(new GenericInterceptor()))
	            .make()
	            .load(originalBeanClass.getClassLoader())
	            .getLoaded();		
		
		try {
			beanInfo = Introspector.getBeanInfo(originalBeanClass, AbstractEntity.class);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}        
        descriptors = Arrays.asList(beanInfo.getPropertyDescriptors());
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
		
		try {
			constructor = beanClass.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError();
		}
		
		// Initial values must all be null
		T newInstance = createInstance();
		descriptors.forEach(desc -> {
			 try {
				Object defaultValue = desc.getReadMethod().invoke(newInstance);
				if (defaultValue != null) {
					throw new RuntimeException("Non-null default value of property " + desc.getName());
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
		
		
	}

	public Class<T> beanClass() {
		return beanClass;
	}
	
	@Override
	public T createInstance() {
		T newInstance;
		try {
			newInstance = constructor.newInstance();
			newInstance.companion(this);
			return newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

		
	@Override
	public String alias() {
		return alias;
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

	/**
	 * To optimize it, use dynamically generated code!
	 * @param map
	 * @param instance
	 */
	public void transferProperties(Map<String, Object> map, T instance) {
		map.entrySet().forEach(entry -> {
			setProperty(instance, entry.getKey(), entry.getValue());
		});
	}


	@Override
	public Map<String, Object> extractProperties(T instance) {
		Map<String, Object> result = new HashMap<>();
		for (PropertyDescriptor desc : descriptors) {
			try {
				Object value = desc.getReadMethod().invoke(instance);
				result.put(desc.getName(), value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}


	public Object getProperty(AbstractEntity entity, Object key) {
		PropertyDescriptor desc = descriptorMap.get(key);
		if (desc == null) {
			throw new RuntimeException("Invalid bean property: " + key);
		} else {
			try {
				return desc.getReadMethod().invoke(entity);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}	
	}

	public void setProperty(AbstractEntity entity, String key, Object value) {
		PropertyDescriptor desc = descriptorMap.get(key);
		if (desc == null) {
			throw new RuntimeException("Invalid bean property: " + key);
		} else {
			try {
				desc.getWriteMethod().invoke(entity, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}	
	}
	
	
	@Override
	public boolean isMapCompanion() {
		return false;
	}

	@Override
	public void transferProperties(T origInstance, T newInstance) {
		for (PropertyDescriptor desc : descriptors) {
			try {
				Object value = desc.getReadMethod().invoke(origInstance);
				newInstance.put(desc.getName(), value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	Set<Entry<String, Object>> entrySetOf(AbstractEntity entity) {
		Set<Entry<String, Object>> resultSet = new HashSet<>();
		for (PropertyDescriptor descriptor : descriptors) {
				try {
					resultSet.add(new MapEntry(entity, descriptor.getName(), 
							descriptor.getReadMethod().invoke(entity)));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
		};
		
		return resultSet;
	}

	public int propertyCount() {
		return descriptors.size();
	}








}
