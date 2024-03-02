package de.protubero.beanstore.entity;

import java.util.Objects;
import java.util.function.BiConsumer;

import de.protubero.beanstore.entity.AbstractPersistentObject.State;

public final class MapObjectCompanion extends AbstractCompanion<MapObject> {

	private String alias;

	
	MapObjectCompanion(String alias) {
		this.alias = Objects.requireNonNull(alias);
	}

	
	@Override
	public String alias() {
		return alias;
	}

	@Override
	public MapObject createInstance() {
		MapObject result = new MapObject(State.INSTANTIATED);
		result.companion(this);
		return result;
	}

	@Override
	public MapObject createUnmanagedInstance() {
		MapObject result = new MapObject(State.UNMANAGED);
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


	@Override
	public void forEachProperty(MapObject instance, BiConsumer<String, Object> consumer) {
		instance.entrySet().forEach(entry -> {
			consumer.accept(entry.getKey(), entry.getValue());
		});
	}



}
