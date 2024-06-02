package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import org.pcollections.PSet;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.kryo.KryoDictionary;

public class LabelUpdateSetKryoSerializer extends Serializer<LabelUpdateSet> {

	private KryoDictionary dictionary; 
	

	public LabelUpdateSetKryoSerializer(KryoDictionary dictionary) {
		this.dictionary = Objects.requireNonNull(dictionary);
	}
	
	@Override
	public void write(Kryo kryo, Output output, LabelUpdateSet object) {
		
		output.writeVarInt(object.getPositiveSet().size(), true);
		for (var elt : object.getPositiveSet()) {
			output.writeInt(dictionary.getOrCreate((String) elt));
		}
		
		output.writeVarInt(object.getNegativeSet().size(), true);
		for (var elt : object.getNegativeSet()) {
			output.writeInt(dictionary.getOrCreate((String) elt));
		}
	}

	@Override
	public LabelUpdateSet read(Kryo kryo, Input input, Class<? extends LabelUpdateSet> type) {
		PSet<String> result = LabelUpdateSet.empty();
		
		int posCount = input.readInt(true);
		for (int i = 0; i < posCount; i++) {
			String label = dictionary.textByCode(input.readInt(true));
			result = result.plus(label);
		}

		int negCount = input.readInt(true);
		for (int i = 0; i < negCount; i++) {
			String label = dictionary.textByCode(input.readInt(true));
			result = result.minus(label);
		}
		
		return (LabelUpdateSet) result;
	}

}
