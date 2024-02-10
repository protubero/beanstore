package de.protubero.beanstore.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MapObjectCompanion extends AbstractCompanion<MapObject> {

	private String alias;

	private static Map<String, MapObjectCompanion> companionMap = new HashMap<>();
	
	private MapObjectCompanion(String alias) {
		this.alias = Objects.requireNonNull(alias);
	}

	public static synchronized MapObjectCompanion getOrCreate(String alias) {
		MapObjectCompanion result = companionMap.get(Objects.requireNonNull(alias));
		if (result == null) {
			result = new MapObjectCompanion(alias);
			companionMap.put(alias, result);
		}
		return result;
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
		throw new UnsupportedOperationException();
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
