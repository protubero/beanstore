package de.protubero.beanstore.base.entity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ObjectArraySerializer;

import de.protubero.beanstore.persistence.api.CustomValueType;
import de.protubero.beanstore.persistence.base.Tag;
import de.protubero.beanstore.persistence.base.TagGroup;

public final class Tags implements CustomValueType {

	//public static final Tags EMPTY = new Tags();
	
	public DefaultArraySerializers.ObjectArraySerializer OBJECT_ARRAY_SERIALIZER;
	private static Tag[] DUMMY = new Tag[] {};
	
	private PSet<Tag> tagSet;
	private AbstractTaggedEntity entity;

	public Tags(AbstractTaggedEntity anEntity, PSet<Tag> tagSet) {
		this.entity = Objects.requireNonNull(anEntity);
		this.tagSet = Objects.requireNonNull(tagSet);
	}

	public Tags(AbstractTaggedEntity anEntity) {
		this.entity = Objects.requireNonNull(anEntity);
		this.tagSet = HashTreePSet.empty();
	}
	
	public Tags() {
		
	}

	
	public int size() {
		return tagSet.size();
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		Tag[] tagArray = tagSet.toArray(new Tag[tagSet.size()]);

		ObjectArraySerializer serializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY.getClass());
		serializer.setAcceptsNull(true);
		serializer.setElementsAreSameType(true);
		serializer.setElementsCanBeNull(false);

		serializer.write(kryo, output, tagArray);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		ObjectArraySerializer serializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY.getClass());
		serializer.setAcceptsNull(true);
		serializer.setElementsAreSameType(true);
		serializer.setElementsCanBeNull(false);

		Tag[] tagArray = (Tag[]) serializer.read(kryo, input, DUMMY.getClass());
		tagSet = HashTreePSet.from(List.of(tagArray));
	}	
	
	public void plus(Tag aTag) {
		entity.setTags(new Tags(entity, tagSet.plus(aTag)));
	}

	public void plus(Tag ... tags) {
		entity.setTags(new Tags(entity, tagSet.plusAll(List.of(tags))));
	}
	
	public void minus(Tag aTag) {
		entity.setTags(new Tags(entity, tagSet.minus(aTag)));
	}
	
	public void minus(Tag ... tags) {
		entity.setTags(new Tags(entity, tagSet.minusAll(List.of(tags))));
	}
	
	@Override
	public String toString() {
		return tagSet.toString();
	}

	public void setEntity(AbstractTaggedEntity entity) {
		this.entity = entity;
	}

	public boolean contains(Tag tag) {
		return tagSet.contains(tag);
	}

	public Optional<Tag> findFirst(TagGroup group) {
		return tagSet.stream().filter(tag -> tag.group() == group).findFirst();
	}
	
}
