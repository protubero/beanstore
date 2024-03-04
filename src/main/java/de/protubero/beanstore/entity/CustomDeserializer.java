package de.protubero.beanstore.entity;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;

@SuppressWarnings("serial")
public class CustomDeserializer extends StdDeserializer<AbstractEntity> implements ContextualDeserializer {

	private static Map<Class<? extends AbstractEntity>, ContextualEntityDeserializer<? extends AbstractEntity>> deserializerMap = new HashMap<>();
	
	static class ContextualEntityDeserializer<T extends AbstractEntity> extends StdDeserializer<T> {

		public ContextualEntityDeserializer(Class<T> vc) {
			super(vc);
		}

		@Override
		public T deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JacksonException {
			JsonNode node = jp.getCodec().readTree(jp);
	        
			if (!node.isObject()) {
	        	throw new RuntimeException("json object expected");
			}
			
			Long id = null;
			
			LongNode longNode = (LongNode) node.get("_id");
			if (longNode != null) {
				id = (Long) longNode.numberValue();
			}
			
			String type = null;
	        JsonNode typeNode = node.get("_type");
	        if (typeNode != null) {
				type = typeNode.asText();
		        if (type != null && type.trim().length() == 0) {
		        	throw new RuntimeException("_type must not be empty");
		        }
	        }    

	        EntityCompanion<T> companion = null;
	        
	        if (type != null) {
		        Optional<EntityCompanion<T>> companionOpt = CompanionRegistry.getEntityCompanionByAlias(type);
		        if (companionOpt.isEmpty()) {
		        	throw new RuntimeException("Invalid entity type " + type);
		        }
		        
		        companion = companionOpt.get();
	        } else {
	        	@SuppressWarnings("unchecked")
				Optional<EntityCompanion<T>> companionOpt = CompanionRegistry.getEntityCompanionByClass((Class<T>) handledType());
		        if (companionOpt.isEmpty()) {
		        	throw new AssertionError();
		        }
		        
		        companion = companionOpt.get();
	        }
	        
			T o = companion.createUnmanagedInstance();
	        if (id != null) {
	        	o.id(id);
	        }
	        
	        Iterator<Entry<String, JsonNode>> fieldIterator = node.fields();
	        while (fieldIterator.hasNext()) {
	        	Entry<String, JsonNode> entry = fieldIterator.next();
	        	if (!entry.getKey().equals("_id") && !entry.getKey().equals("_type")) {
	        		PropertyDescriptor propertyDesc = companion.propertyDescriptorOf(entry.getKey());
	        		if (propertyDesc == null) {
	        			throw new RuntimeException("Invalid data bean (" + type +") property: " + entry.getKey());
	        		}
	        		
	        		Object value = ctxt.readTreeAsValue(entry.getValue(), propertyDesc.getPropertyType());
	        		try {
						propertyDesc.getWriteMethod().invoke(o, value);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
	        	}
	        }
	        
	        return o;
	    }
		
	}
	
	protected CustomDeserializer() {
		super(AbstractPersistentObject.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		
		Class<?> resolvedType = ctxt.getContextualType().getRawClass();
		System.out.println(resolvedType);
		ContextualEntityDeserializer<? extends AbstractEntity> deserializer = deserializerMap.get(resolvedType);
		if (deserializer == null) {
			deserializer = new ContextualEntityDeserializer(resolvedType);
			deserializerMap.put((Class) resolvedType, deserializer);
		}
		return deserializer;
	}

	@Override
	public AbstractEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		throw new AssertionError();
	}

	
}
