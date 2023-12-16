package de.protubero.beanstore.plugins.tags;

import java.util.List;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ObjectArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

public class TagsSerializer extends ImmutableSerializer<Tags> {

	public DefaultArraySerializers.ObjectArraySerializer OBJECT_ARRAY_SERIALIZER;
	private static Tag[] DUMMY = new Tag[] {};
	
	
	@Override
	public void write(Kryo kryo, Output output, Tags tags) {
		Tag[] tagArray = tags.tagSet.toArray(new Tag[tags.tagSet.size()]);

		ObjectArraySerializer serializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY.getClass());
		serializer.setAcceptsNull(true);
		serializer.setElementsAreSameType(true);
		serializer.setElementsCanBeNull(false);

		serializer.write(kryo, output, tagArray);
	}

	@Override
	public Tags read(Kryo kryo, Input input, Class<? extends Tags> type) {
		ObjectArraySerializer serializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY.getClass());
		serializer.setAcceptsNull(true);
		serializer.setElementsAreSameType(true);
		serializer.setElementsCanBeNull(false);

		Tag[] tagArray = (Tag[]) serializer.read(kryo, input, DUMMY.getClass());
		MapPSet<Tag> tagSet = HashTreePSet.from(List.of(tagArray));
		return new Tags(tagSet);
	}

}
