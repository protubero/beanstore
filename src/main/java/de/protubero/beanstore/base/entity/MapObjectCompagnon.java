package de.protubero.beanstore.base.entity;

public final class MapObjectCompagnon extends AbstractCompagnon<MapObject> {

	private String alias;

	public MapObjectCompagnon(String alias) {
		this.alias = alias;
	}

	@Override
	public String alias() {
		return alias;
	}

	@Override
	public MapObject createInstance() {
		MapObject result = new MapObject();
		result.compagnon(this);
		return result;
	}

	@Override
	public Class<MapObject> entityClass() {
		return MapObject.class;
	}

}
