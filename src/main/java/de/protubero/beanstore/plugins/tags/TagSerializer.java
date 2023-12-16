package de.protubero.beanstore.plugins.tags;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

public class TagSerializer extends ImmutableSerializer<Tag> {

	@Override
	public void write(Kryo kryo, Output output, Tag tag) {
		output.writeString(tag.group().name());
		output.writeString(tag.name());
	}

	@Override
	public Tag read(Kryo kryo, Input input, Class<? extends Tag> type) {
		String groupName = input.readString();
		String tagName = input.readString();
		Tag tag = TagManager.instance().groupAuto(groupName).tagAuto(tagName);
		return tag;
	}

}
