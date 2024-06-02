package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.persistence.kryo.KryoDictionary;

public class LinkValueKryoSerializer  extends Serializer<LinkValue> {

	private KryoDictionary dictionary;
	
	public LinkValueKryoSerializer(KryoDictionary dictionary) {
		this.dictionary = Objects.requireNonNull(dictionary);
	}

	@Override
	public void write(Kryo kryo, Output output, LinkValue object) {
		output.writeInt(dictionary.codeByText(object.getKey().alias()), true);
		output.writeLong(object.getKey().id(), true);
		output.writeInt(dictionary.codeByText(object.getType()), true);
	}

	@Override
	public LinkValue read(Kryo kryo, Input input, Class<? extends LinkValue> clazz) {
		String alias = dictionary.textByCode(input.readInt(true));
		long id = input.readLong(true);
		String type = dictionary.textByCode(input.readInt(true));

		return new LinkValue(PersistentObjectKey.of(alias, id), type);
	}


}
