package de.protubero.beanstore.base;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class BeanStoreInstanceSerializer extends StdSerializer<AbstractPersistentObject>{

	public BeanStoreInstanceSerializer() {
		super(AbstractPersistentObject.class);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6607540368086879554L;

	@Override
	public void serialize(AbstractPersistentObject obj, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeStartObject();
        gen.writeNumberField("_id", obj.id);
        gen.writeStringField("_type", obj.alias());
        
        obj.entrySet().forEach(entry -> {
        	if (entry.getValue() != null) {
            	try {
					JsonSerializer<Object> vs = provider.findValueSerializer(entry.getValue().getClass());
					gen.writeFieldName(entry.getKey());
					vs.serialize(entry.getValue(), gen, provider);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
        		
        	}
        });
        
        gen.writeEndObject();
	}

}
