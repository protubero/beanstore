package de.protubero.beanstore.persistence.kryo;

import java.io.OutputStream;
import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class KryoOutput extends Output {

	private KryoDictionary dictionary;
	
	public KryoOutput (OutputStream outputStream, KryoDictionary dictionary) {
		super(outputStream);
		
		this.dictionary = Objects.requireNonNull(dictionary);
	}
	
	public void writeStringDict(String value) throws KryoException {
		Integer code = dictionary.codeByText(value);
		if (code == null) {
			throw new RuntimeException("Kryo Dictionary Error");
		}
		
		writeVarInt(code, true);
	}
	
	
	
}
