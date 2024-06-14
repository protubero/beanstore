package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import org.pcollections.PSet;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.persistence.kryo.KryoDictionary;

public class LinkValueUpdateSetKryoSerializer extends Serializer<LinkValueUpdateSet> {

	private KryoDictionary dictionary; 
	

	public LinkValueUpdateSetKryoSerializer(KryoDictionary dictionary) {
		this.dictionary = Objects.requireNonNull(dictionary);
	}
	
	@Override
	public void write(Kryo kryo, Output output, LinkValueUpdateSet object) {
		
		output.writeVarInt(object.getPositiveSet().size(), true);
		for (var elt : object.getPositiveSet()) {
			LinkValue lv = (LinkValue) elt;
			output.writeInt(dictionary.getOrCreate(lv.getType()), true);
			output.writeInt(dictionary.getOrCreate(lv.getKey().alias()), true);
			output.writeLong(lv.getKey().id());
		}
		
		output.writeVarInt(object.getNegativeSet().size(), true);
		for (var elt : object.getNegativeSet()) {
			LinkValue lv = (LinkValue) elt;
			output.writeInt(dictionary.getOrCreate(lv.getType()), true);
			output.writeInt(dictionary.getOrCreate(lv.getKey().alias()), true);
			output.writeLong(lv.getKey().id());
		}
	}

	@Override
	public LinkValueUpdateSet read(Kryo kryo, Input input, Class<? extends LinkValueUpdateSet> type) {
		PSet<LinkValue> result = LinkValueUpdateSet.empty();
		
		int posCount = input.readInt(true);
		for (int i = 0; i < posCount; i++) {
			String linkType = dictionary.textByCode(input.readInt(true));
			String alias = dictionary.textByCode(input.readInt(true));
			long id = input.readVarLong(true);
			result = result.plus(LinkValue.of(alias, id, linkType));
		}

		int negCount = input.readInt(true);
		for (int i = 0; i < negCount; i++) {
			String linkType = dictionary.textByCode(input.readInt(true));
			String alias = dictionary.textByCode(input.readInt(true));
			long id = input.readVarLong(true);
			result = result.minus(LinkValue.of(alias, id, linkType));
		}
		
		return (LinkValueUpdateSet) result;
	}

}
