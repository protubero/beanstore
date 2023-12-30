package de.protubero.beanstore.plugins.keyvalue;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

public class KeyObjectSerializer extends ImmutableSerializer<KeyObject> {

	@Override
	public void write(Kryo kryo, Output output, KeyObject object) {
		
	}

	@Override
	public KeyObject read(Kryo kryo, Input input, Class<? extends KeyObject> type) {
		// TODO Auto-generated method stub
		return null;
	}

}
