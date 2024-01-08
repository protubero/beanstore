package de.protubero.beanstore.persistence.kryo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class KryoDictionary {

	class DictionaryEntry {

		private Integer code;
		private String text;

		public DictionaryEntry(Integer code, String text) {
			this.code = Objects.requireNonNull(code);
			this.text = Objects.requireNonNull(text);
		}

		public Integer getCode() {
			return code;
		}

		public String getText() {
			return text;
		}

		
	}
	
	
	private Map<String, Integer> stringToCodeMap = new HashMap<>();
	private Map<Integer, String> codeToStringMap = new HashMap<>();
	
	private List<DictionaryEntry> newEntries = new ArrayList<>();
	
	
	private int generator = 100;
	

	private Integer generateNewEntry(String text) {
		Integer code = generator++;
		newEntries.add(new DictionaryEntry(code, text));

		stringToCodeMap.put(text, code);
		codeToStringMap.put(code, text);
		
		return code;
	}
	
	public boolean hasNewEntries() {
		return newEntries.size() > 0;
	}
	
	public void writeNewEntries(Output output) {
		output.writeInt(newEntries.size(), true);
		for (DictionaryEntry entry : newEntries) {
			output.writeInt(entry.getCode().intValue(), true);
			output.writeString(entry.getText());
		}
		
		newEntries.clear();
	}
	
	public void load(Input input) {
		int entryNum = input.readInt(true);
		
		for (int i = 0; i < entryNum; i++) {
			int code = input.readInt(true);
			String text = input.readString();
			
			stringToCodeMap.put(text, code);
			codeToStringMap.put(code, text);
			
			if (code >= generator) {
				generator = code + 1;
			}
		}
	}
	
	public String textByCode(Integer value) {
		return codeToStringMap.get(Objects.requireNonNull(value));
	}

	public Integer codeByText(String text) {
		if (text == null) {
			return 0;
		}
		return stringToCodeMap.get(text);
	}

	public Integer getOrCreate(String text) {
		Integer result = codeByText(text);
		
		if (result == null) {
			return generateNewEntry(text);
		} else {
			return result;
		}
	}

	public int newEntriesCount() {
		return newEntries.size();
	}

}
