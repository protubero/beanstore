package de.protubero.beanstore.persistence.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PersistentInstanceTransaction {

	public static final int TYPE_CREATE = 0;
	public static final int TYPE_UPDATE = 1;
	public static final int TYPE_DELETE = 2;

	private int type;
	private String alias;
	private Long id;
	
	// use for optimistic locking
	@JsonIgnore
	private transient Object ref;

	private PersistentProperty[] propertyUpdates;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if (this.id != null) {
			throw new AssertionError();
		}
		this.id = id;
	}

	public PersistentProperty[] getPropertyUpdates() {
		return propertyUpdates;
	}

	public void setPropertyUpdates(PersistentProperty[] propertyUpdates) {
		this.propertyUpdates = propertyUpdates;
	}

	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}
	
	@Override
	public String toString() {
		String updates = "";
		if (propertyUpdates != null) {
			for (int i = 0; i < propertyUpdates.length; i++) {
				if (i > 0) {
					updates += ", ";
				}
				updates += propertyUpdates[i];
			}
		}
		return typeAsString(type) + "-" + alias + "[" + id + "] " + updates;
	}

	public static String typeAsString(int aType) {
		switch (aType) {
		case TYPE_CREATE:
			return "create";
		case TYPE_UPDATE:
			return "update";
		case TYPE_DELETE:
			return "delete";
		default:
			throw new AssertionError();
		}
	}
	
}
