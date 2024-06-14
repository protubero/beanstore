package de.protubero.beanstore.entity;

import java.io.IOException;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@SuppressWarnings("serial")
public class CustomSerializer extends StdSerializer<AbstractPersistentObject> {

	public CustomSerializer() {
		super(AbstractPersistentObject.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void serialize(AbstractPersistentObject object, JsonGenerator gen, SerializerProvider provider)
			throws IOException {

		gen.writeStartObject();
		gen.writePOJOField("_type", object.alias()); 
		gen.writePOJOField("_id", object.id()); 
		gen.writePOJOField("_version", object.version()); 
		gen.writePOJOField("_state", object.state().ordinal()); 
		((Companion) object.companion()).forEachProperty(object, new BiConsumer<String, Object>() {

			@Override
			public void accept(String name, Object value) {
				try {
					gen.writePOJOField(name, value);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
        gen.writeEndObject();	
	}

}
