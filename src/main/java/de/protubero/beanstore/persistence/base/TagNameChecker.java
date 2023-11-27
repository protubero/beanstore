package de.protubero.beanstore.persistence.base;

public class TagNameChecker {

	private static boolean isNonAlphanumericAnyLangScript(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '_') && (c != '-')) {
                return true;
            }
        }
        return false;
    }

	public static void throwIfContainsInvalidChar(String str) {
		if (str == null) {
			throw new RuntimeException("tag or tag group name must not ne null");
		}
		if (isNonAlphanumericAnyLangScript(str)) {
			throw new RuntimeException("invalid character in tag or tag group name: " + str);
		}
	}
	
}
