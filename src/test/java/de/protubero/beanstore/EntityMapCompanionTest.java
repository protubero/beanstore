package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.MapObject;
import de.protubero.beanstore.base.entity.MapObjectCompanion;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;


public class EntityMapCompanionTest {

	@Test
	public void test() {
		MapObjectCompanion companion = new MapObjectCompanion("employee");
		assertEquals("employee", companion.alias());

		// create new object and check initial values
		final MapObject newInstance = companion.createInstance(44);
		assertSame(companion, newInstance.companion());
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
		assertSame(companion, detached.companion());
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
		final MapObject otherInstance = companion.createInstance();
		otherInstance.applyTransition(Transition.INSTANTIATED_TO_NEW);
		assertSame(companion, otherInstance.companion());
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
