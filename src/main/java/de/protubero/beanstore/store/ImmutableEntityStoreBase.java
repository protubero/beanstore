package de.protubero.beanstore.store;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public class ImmutableEntityStoreBase<T extends AbstractPersistentObject> {

	public static final Logger log = LoggerFactory.getLogger(ImmutableEntityStoreBase.class);

	private Map<Long, T> objectMap;

	private Companion<T> companion;
	
	private long nextInstanceId = 0L;


	public static Logger getLog() {
		return log;
	}

	public Map<Long, T> getObjectMap() {
		return objectMap;
	}

	public Companion<T> getCompanion() {
		return companion;
	}

	public long getNextInstanceId() {
		return nextInstanceId;
	}

	public void setObjectMap(Map<Long, T> objectMap) {
		this.objectMap = objectMap;
	}

	public void setCompanion(Companion<T> companion) {
		this.companion = companion;
	}

	public void setNextInstanceId(long nextInstanceId) {
		this.nextInstanceId = nextInstanceId;
	}

	
	
	
}
