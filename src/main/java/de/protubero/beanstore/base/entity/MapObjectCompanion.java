package de.protubero.beanstore.base.entity;

import java.util.Map;
import java.util.Objects;

public final class MapObjectCompanion extends AbstractCompanion<MapObject> {

	private String alias;

	public MapObjectCompanion(String alias) {
		this.alias = Objects.requireNonNull(alias);
	}

	@Override
	public String alias() {
		return alias;
	}

	@Override
	public MapObject createInstance() {
		MapObject result = new MapObject();
		result.companion(this);
		return result;
	}

	@Override
	public Class<MapObject> entityClass() {
		return MapObject.class;
	}

	@Override
	public Map<String, Object> extractProperties(MapObject instance) {
		return instance;
	}

	@Override
	public boolean isMapCompanion() {
		return true;
	}

	@Override
	public void transferProperties(MapObject origInstance, MapObject newInstance) {
		newInstance.putAll(origInstance);
	}

}
