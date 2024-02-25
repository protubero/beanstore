package de.protubero.beanstore.persistence.api;

public class PersistentInstanceTransaction {

	public static final byte TYPE_CREATE = 0;
	public static final byte TYPE_UPDATE = 1;
	public static final byte TYPE_DELETE = 2;

	private byte type;
	private String alias;
	private Long id;
	private int version;
	

	private PersistentProperty[] propertyUpdates;

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
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

	public static String typeAsString(byte aType) {
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
}
