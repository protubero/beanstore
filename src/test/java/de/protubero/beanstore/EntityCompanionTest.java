package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.EntityCompanion;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.model.Employee;

public class EntityCompanionTest {

	@Test
	public void map_like_test() {
		EntityCompanion<Employee> companion = new EntityCompanion<>(Employee.class);
		assertEquals("employee", companion.alias());

		// create new object and check initial values
		final Employee newInstance = companion.createInstance(44);
		assertSame(companion, newInstance.companion());
		assertEquals(null, newInstance.refInstance());
		assertEquals(null, newInstance.changes());
		assertEquals(44, newInstance.id());
		assertEquals("employee", newInstance.alias());
		assertEquals(0, newInstance.size());
		assertEquals(State.INSTANTIATED, newInstance.state());
		
		
		// set and read values in INSTANTIATED state
		assertEquals(null, newInstance.get("age"));
		newInstance.put("firstName", "Erik");
		assertEquals("Erik", newInstance.get("firstName"));
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
		final Employee otherInstance = companion.createInstance();
		otherInstance.applyTransition(Transition.INSTANTIATED_TO_NEW);
		assertSame(companion, otherInstance.companion());
		assertNull(otherInstance.refInstance());
		assertEquals(null, otherInstance.changes());
		assertThrows(Exception.class, () -> {otherInstance.id();});
		assertEquals("employee", otherInstance.alias());
		assertEquals(0, otherInstance.size());
		assertEquals(State.NEW, otherInstance.state());
		
		// change properties of NEW object
		otherInstance.put("age", 55);
		assertEquals(55, otherInstance.get("age"));
		assertEquals(1, otherInstance.changes().size());
		assertEquals(55, otherInstance.changes().get("age"));
		
	}
	
	@Test
	public void bean_like_test() {
		EntityCompanion<Employee> companion = new EntityCompanion<>(Employee.class);
		assertEquals("employee", companion.alias());

		// create new object and check initial values
		final Employee newInstance = companion.createInstance(44);
		
		// set and read values in INSTANTIATED state
		assertEquals(null, newInstance.getAge());
		newInstance.setFirstName("Erik");
		assertEquals("Erik", newInstance.getFirstName());
		assertEquals("Erik", newInstance.get("firstName"));
		newInstance.setAge(33);
		assertEquals(33, newInstance.getAge());
		assertEquals(33, newInstance.get("age"));
		assertEquals(null, newInstance.changes());
		
		// INSTANTIATED -> READY
		newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
		assertThrows(Exception.class, () -> {newInstance.setAge(4);});

		// create DETACHED object 
		Employee detached = newInstance.detach();
		assertSame(companion, detached.companion());
		assertEquals(newInstance, detached.refInstance());
		assertEquals(null, detached.changes());
		assertEquals(44, detached.id());
		assertEquals("employee", detached.alias());
		assertEquals(2, detached.size());
		assertEquals(State.DETACHED, detached.state());
		assertEquals(33, detached.getAge());

		// change properties of detached object
		detached.put("age", 55);
		assertEquals(55, detached.getAge());
		assertEquals(1, detached.changes().size());
		assertEquals(55, detached.changes().get("age"));
		
		
		// READY -> OUTDATED
		newInstance.applyTransition(Transition.READY_TO_OUTDATED);
		assertThrows(Exception.class, () -> {newInstance.setAge(3);});
		assertNull(newInstance.changes());
				
		// NEW
		final Employee otherInstance = companion.createInstance();
		otherInstance.applyTransition(Transition.INSTANTIATED_TO_NEW);
		assertSame(companion, otherInstance.companion());
		assertNull(otherInstance.refInstance());
		assertEquals(null, otherInstance.changes());
		assertThrows(Exception.class, () -> {otherInstance.id();});
		assertEquals("employee", otherInstance.alias());
		assertEquals(0, otherInstance.size());
		assertEquals(State.NEW, otherInstance.state());
		
		// change properties of NEW object
		otherInstance.setAge(55);
		assertEquals(55, otherInstance.getAge());
		assertEquals(1, otherInstance.changes().size());
		assertEquals(55, otherInstance.changes().get("age"));
		
	}
}
