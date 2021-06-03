package de.protubero.beanstore.base.entity;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public final class EntityCompagnon<T extends AbstractEntity> extends AbstractCompagnon<T> {

	public static final Logger log = LoggerFactory.getLogger(EntityCompagnon.class);
	
	private Kryo kryo;
	private Class<T> beanClass;
	private BeanInfo beanInfo;
	private List<PropertyDescriptor> descriptors;
	private Map<String, PropertyDescriptor> descriptorMap;
	private String alias;
	private Class<T> originalBeanClass;
	
	@SuppressWarnings("unchecked")
	public EntityCompagnon(Class<T> originalBeanClass) {
		this.originalBeanClass = originalBeanClass;
		Entity entityAnnotytion = originalBeanClass.getAnnotation(Entity.class);
		alias = entityAnnotytion.alias();
		
		this.beanClass = (Class<T>) new ByteBuddy().subclass(originalBeanClass)
	            .method(ElementMatchers.isSetter())
	            .intercept(MethodDelegation.to(new GenericInterceptor()))
	            .make()
	            .load(EntityCompagnon.class.getClassLoader())
	            .getLoaded();		
		
		try {
			beanInfo = Introspector.getBeanInfo(beanClass, AbstractEntity.class);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}        
        descriptors = Arrays.asList(beanInfo.getPropertyDescriptors());
        descriptorMap = new HashMap<>();
		descriptors.forEach(desc -> {
			log.info("bean property " + desc.getName());
			
			descriptorMap.put(desc.getName(), desc);
		});
		
		kryo = new Kryo();
		
		kryo.register(beanClass);
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
			newInstance.compagnon(this);
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



}
