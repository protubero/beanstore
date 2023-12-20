package de.protubero.beanstore.model;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;


public class PostCodeSerializer extends ImmutableSerializer<PostCode>{

	@Override
	public void write(Kryo kryo, Output output, PostCode object) {
		output.write(null);
	}

	@Override
	public PostCode read(Kryo kryo, Input input, Class<? extends PostCode> type) {
		return null;
	}

}
