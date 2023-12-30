package de.protubero.beanstore.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KryoDictionary {

	private Map<String, Integer> stringToCodeMap = new HashMap<>();
	private Map<Integer, String> codeToStringMap = new HashMap<>();
	
	private int generator = 100;
	
	public Integer addEntry(String text) {
		Integer code = generator++;
		stringToCodeMap.put(text, code);
		codeToStringMap.put(code, text);
		return code;
	}
	
	public String textByCode(Integer value) {
		return codeToStringMap.get(Objects.requireNonNull(value));
	}

	public Integer codeByText(String text) {
		if (text == null) {
			return 0;
		}
		return stringToCodeMap.get(Objects.requireNonNull(text));
	}

	public Integer getOrAdd(String value) {
		Integer code = codeByText(value);
		if (code == null) {
			code = addEntry(value);
		}
		return code;
	}
}
