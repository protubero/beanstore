package de.protubero.beanstore.base;

public final class EntityMapCompagnon extends AbstractCompagnon<EntityMap> {

	private String alias;

	public EntityMapCompagnon(String alias) {
		this.alias = alias;
	}

	@Override
	public String alias() {
		return alias;
	}

	@Override
	public EntityMap createInstance() {
		EntityMap result = new EntityMap();
		result.compagnon(this);
		return result;
	}

	@Override
	public Class<EntityMap> entityClass() {
		return EntityMap.class;
	}

}
