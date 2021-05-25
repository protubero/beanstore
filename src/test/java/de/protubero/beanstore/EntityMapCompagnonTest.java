package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.MapObject;
import de.protubero.beanstore.base.MapObjectCompagnon;
import de.protubero.beanstore.base.AbstractPersistentObject.State;
import de.protubero.beanstore.base.AbstractPersistentObject.Transition;


public class EntityMapCompagnonTest {

	@Test
	public void test() {
		MapObjectCompagnon compagnon = new MapObjectCompagnon("employee");
		assertEquals("employee", compagnon.alias());

		// create new object and check initial values
		final MapObject newInstance = compagnon.createInstance(44);
		assertSame(compagnon, newInstance.compagnon());
		assertEquals(null, newInstance.refInstance());
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
		newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
		assertThrows(Exception.class, () -> {newInstance.put("value", "test");});

		// create DETACHED object 
		AbstractPersistentObject detached = newInstance.detach();
		assertSame(compagnon, detached.compagnon());
		assertEquals(newInstance, detached.refInstance());
		assertEquals(null, detached.changes());
		assertEquals(44, detached.id());
		assertEquals("employee", detached.alias());
		assertEquals(2, detached.size());
		assertEquals(State.DETACHED, detached.state());
		assertEquals(33, detached.get("age"));
		

		// change properties of detached object
		detached.put("age", 55);
		assertEquals(55, detached.get("age"));
		assertEquals(1, detached.changes().size());
		assertEquals(55, detached.changes().get("age"));
		
		
		// READY -> OUTDATED
		newInstance.applyTransition(Transition.READY_TO_OUTDATED);
		assertThrows(Exception.class, () -> {newInstance.put("value", "test");});
		assertNull(newInstance.changes());
		
		System.out.println(newInstance);

				
		// NEW
		final MapObject otherInstance = compagnon.createInstance();
		otherInstance.applyTransition(Transition.INSTANTIATED_TO_NEW);
		assertSame(compagnon, otherInstance.compagnon());
		assertNull(otherInstance.refInstance());
		assertEquals(null, otherInstance.changes());
		assertEquals(null, otherInstance.id());
		assertEquals("employee", otherInstance.alias());
		assertEquals(0, otherInstance.size());
		assertEquals(State.NEW, otherInstance.state());
		
		// change properties of NEW object
		otherInstance.put("age", 55);
		assertEquals(55, otherInstance.get("age"));
		assertEquals(1, otherInstance.changes().size());
		assertEquals(55, otherInstance.changes().get("age"));
		
	}
	
}
