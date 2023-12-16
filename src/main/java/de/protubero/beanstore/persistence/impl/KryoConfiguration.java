package de.protubero.beanstore.persistence.impl;

import java.time.Instant;
import java.util.Iterator;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.TransactionWriter;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentPropertyUpdate;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.plugins.tags.Tag;
import de.protubero.beanstore.plugins.tags.TagSerializer;

public class KryoConfiguration {

	private Kryo kryo;

	public KryoConfiguration() {
		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.setWarnUnregisteredClasses(true);
		
		kryo.register(PersistentTransaction.class, 20);
		kryo.register(PersistentInstanceTransaction.class, 21);
		kryo.register(PersistentInstanceTransaction[].class, 22);
		kryo.register(PersistentPropertyUpdate[].class, 23);
		kryo.register(PersistentPropertyUpdate.class, 24);
		kryo.register(Instant.class, 25);
		
		kryo.addDefaultSerializer(Object.class, KryoDefaultSerializer.class);
	}
	
	public Kryo getKryo() {
		return kryo;
	}
	
	
	
}
