package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.entity.PersistentObjectKey;

public final class LinkValue {

	private PersistentObjectKey<?> key;
	private String type;
	
	private LinkValue(PersistentObjectKey<?> key, String aType) {
		this.key = Objects.requireNonNull(key);
		if (key.alias() == null) {
			throw new RuntimeException("LinkValue must have alias set");
		}
		this.type = aType;
	}

	public PersistentObjectKey<?> getKey() {
		return key;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((LinkValue) obj).key.equals(key) &&
				Objects.equals(((LinkValue) obj).type, type);
	}

	public static LinkValue of(AbstractPersistentObject apo) {
		return new LinkValue(Keys.key(apo), null);
	}
	
	public static LinkValue of(String alias, long id, String type) {
		return new LinkValue(Keys.key(alias, id), type);
	}

	public static LinkValue of(AbstractPersistentObject apo, String type) {
		return new LinkValue(Keys.key(apo), type);
	}
}
