package de.protubero.beanstore.base;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class StoreSnapshot {

	private Map<String, Collection<AbstractPersistentObject>> snapshotMap;
	
	public StoreSnapshot(Map<String, Collection<AbstractPersistentObject>> snapshotMap) {
		this.snapshotMap = Objects.requireNonNull(snapshotMap);
	}

	public Map<String, Collection<AbstractPersistentObject>> getSnapshotMap() {
		return snapshotMap;
	}

	
	
}
