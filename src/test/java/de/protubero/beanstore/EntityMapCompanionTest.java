package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;


public class EntityMapCompanionTest {

	@Test
	public void test() {
		/*
		MapObjectCompanion companion = new MapObjectCompanion("employee");
		assertEquals("employee", companion.alias());

		// create new object and check initial values
		final MapObject newInstance = companion.createInstance(44);
		assertSame(companion, newInstance.companion());
		assertEquals(null, newInstance.changes());
		assertEquals(44, newInstance.id());
		assertEquals("employee", newInstance.alias());
		assertEquals(0, newInstance.size());
		assertEquals(State.INSTANTIATED, newInstance.state());
		
		
		// set and read values in INSTANTIATED state
		assertEquals(null, newInstance.get("age"));
		newInstance.put("name", "Erik");
		assertEquals("Erik", newInstance.get("name"));
		newInstance.put("age", 33);
		assertEquals(33, newInstance.get("age"));
		assertEquals(null, newInstance.changes());
		
		// INSTANTIATED -> READY
		newInstance.state(State.STORED);
		assertThrows(Exception.class, () -> {newInstance.put("value", "test");});

		
		
		// READY -> OUTDATED
		newInstance.state(State.OUTDATED);
		assertThrows(Exception.class, () -> {newInstance.put("value", "test");});
		assertNull(newInstance.changes());
		
		System.out.println(newInstance);

				
		/*
		// NEW
		final MapObject otherInstance = companion.createInstance();
		otherInstance.state(State.PREPARE);;
		assertSame(companion, otherInstance.companion());
		assertEquals(null, otherInstance.changes());
		assertNull(otherInstance.id());
		assertEquals("employee", otherInstance.alias());
		assertEquals(0, otherInstance.size());
		assertEquals(State.PREPARE, otherInstance.state());
		
		// change properties of NEW object
		otherInstance.put("age", 55);
		assertEquals(55, otherInstance.get("age"));
		assertEquals(1, otherInstance.changes().size());
		assertEquals(55, otherInstance.changes().get("age"));
		*/
	}
	
}
