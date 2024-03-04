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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private Constructor<T> managedConstructor;
	private Constructor<T> unmanagedConstructor;

	
	@SuppressWarnings("unchecked")
	EntityCompanion(Class<T> originalBeanClass) {
		if (!AbstractEntity.class.isAssignableFrom(originalBeanClass)) {
			throw new RuntimeException("A data bean must extend AbstractEntity");
		}
		
		try {
			@SuppressWarnings("unused")
			var tConstructor = originalBeanClass.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Missing no-arg constructor in entity class " + originalBeanClass, e);
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
						
			if (AbstractPersistentObject.class.isAssignableFrom(desc.getPropertyType())) {
				throw new RuntimeException("Invalid Property type: " + desc.getPropertyType());
			}
			
			if (desc.getReadMethod() == null) {
				throw new RuntimeException("Property read method not found: " + alias + '.' + desc.getName());
			}
			if (desc.getWriteMethod() == null) {
				throw new RuntimeException("Property write method not found: " + alias + '.' + desc.getName());
			}
			
			descriptorMap.put(desc.getName(), desc);
		});
		
		try {
			managedConstructor = beanClass.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError();
		}

		try {
			unmanagedConstructor = originalBeanClass.getConstructor();
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
			newInstance = managedConstructor.newInstance();
			newInstance.companion(this);
			return newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T createUnmanagedInstance() {
		T newInstance;
		try {
			newInstance = unmanagedConstructor.newInstance();
			return newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void forEachProperty(T object, BiConsumer<String, Object> consumer) {
		for (PropertyDescriptor descriptor : descriptors) {
			 try {
				consumer.accept(descriptor.getName(), descriptor.getReadMethod().invoke(object));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
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
				throw new RuntimeException("error invoking method " + desc.getWriteMethod().getName(), e);
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

	public void transferProperties(Stream<Entry<String, Object>> entryStream, AbstractEntity instance) {
		entryStream.forEach(entry -> {
			setProperty(instance, entry.getKey(), entry.getValue());			
		});
	}

	public PropertyDescriptor propertyDescriptorOf(String property) {
		return descriptorMap.get(property);
	}


}
