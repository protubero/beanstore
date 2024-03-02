package de.protubero.beanstore.entity;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;

@SuppressWarnings("serial")
public class CustomDeserializer extends StdDeserializer<AbstractEntity> {

	protected CustomDeserializer() {
		super(AbstractPersistentObject.class);
	}

	@Override
	public AbstractEntity deserialize(JsonParser jp, DeserializationContext ctxt)
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
		
        JsonNode typeNode = node.get("_type");
        if (typeNode == null) {
        	throw new RuntimeException("_type must not be null");
        }
		String type = typeNode.asText();
        if (type == null || type.trim().length() == 0) {
        	throw new RuntimeException("_type must not be null or empty");
        }

        Optional<EntityCompanion<AbstractEntity>> companionOpt = CompanionRegistry.getEntityCompanionByAlias(type);
        if (companionOpt.isEmpty()) {
        	throw new RuntimeException("Invalid entity type " + type);
        }
        
        EntityCompanion<AbstractEntity> companion = companionOpt.get();
		AbstractEntity o = companion.createUnmanagedInstance();
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
