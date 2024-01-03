package de.protubero.beanstore.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.model.Employee;

public class EntityCompanionTest {

	@Test
	public void testCompanionTypeFlags() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		MapObjectCompanion mapCompanion = new MapObjectCompanion("employee");
		
		assertTrue(entityCompanion.isBean());	
		assertFalse(entityCompanion.isMapCompanion());
		
		assertTrue(mapCompanion.isMapCompanion());	
		assertFalse(mapCompanion.isBean());
		
	}	
	
	@Test
	public void testEntityClassAndAlias() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		MapObjectCompanion mapCompanion = new MapObjectCompanion("employee");

		assertEquals("employee", entityCompanion.alias());
		assertEquals("employee", mapCompanion.alias());
		
		assertEquals(Employee.class, entityCompanion.entityClass());
		assertThrows(UnsupportedOperationException.class, () -> {mapCompanion.entityClass();});
	}	
	
	@Test
	public void createInstanceWithoutId() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		MapObjectCompanion mapCompanion = new MapObjectCompanion("employee");
		
		// create and check instance without id
		final Employee aBeanInstance = entityCompanion.createInstance();
		assertSame(entityCompanion, aBeanInstance.companion());
		assertEquals(null, aBeanInstance.changes());
		assertNull(aBeanInstance.id());
		assertEquals("employee", aBeanInstance.alias());
		assertEquals(4, aBeanInstance.size());
		assertNull(aBeanInstance.get("age"));
		assertNull(aBeanInstance.get("lastName"));
		assertNull(aBeanInstance.get("firstName"));
		assertNull(aBeanInstance.get("employeeNumber"));
		assertEquals(State.INSTANTIATED, aBeanInstance.state());
		
		final MapObject aMapInstance = mapCompanion.createInstance();
		assertSame(mapCompanion, aMapInstance.companion());
		assertEquals(null, aMapInstance.changes());
		assertNull(aMapInstance.id());
		assertEquals("employee", aMapInstance.alias());
		assertEquals(0, aMapInstance.size());
		assertNull(aMapInstance.get("age"));
		assertNull(aMapInstance.get("lastName"));
		assertNull(aMapInstance.get("firstName"));
		assertNull(aMapInstance.get("employeeNumber"));
		assertEquals(State.INSTANTIATED, aBeanInstance.state());
	}
	
	@Test
	public void settingUnknownPropThrowsException() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		final Employee aBeanInstance = entityCompanion.createInstance();
		aBeanInstance.state(State.PREPARE);
		
		aBeanInstance.set("firstName", "John");
		assertEquals("John", aBeanInstance.get("firstName"));
		assertThrows(Exception.class, () -> {aBeanInstance.set("abc", 1);});
	}
	
	@Test
	public void gettingUnknownPropThrowsException() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		final Employee aBeanInstance = entityCompanion.createInstance();
		aBeanInstance.state(State.PREPARE);
		
		aBeanInstance.set("firstName", "John");
		assertEquals("John", aBeanInstance.get("firstName"));
		assertThrows(Exception.class, () -> {aBeanInstance.get("abc");});
	}

	@Test
	public void createInstanceWithId() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		MapObjectCompanion mapCompanion = new MapObjectCompanion("employee");
		
		final Employee aBeanInstance = entityCompanion.createInstance(12);
		assertEquals(12, aBeanInstance.id());
		
		final MapObject aMapInstance = mapCompanion.createInstance(12);
		assertEquals(12, aMapInstance.id());
	}
	
	
	@Test
	public void propChangesAreOnlyAllowedInCertainStates() {
		EntityCompanion<Employee> entityCompanion = new EntityCompanion<>(Employee.class);
		MapObjectCompanion mapCompanion = new MapObjectCompanion("employee");
		
		final Employee aBeanInstance = entityCompanion.createInstance(12);
		final MapObject aMapInstance = mapCompanion.createInstance(12);

		assertEquals(State.INSTANTIATED, aBeanInstance.state());
		
		assertThrows(Exception.class, () -> {aBeanInstance.set("firstName", "Cicero");});
		assertThrows(Exception.class, () -> {aMapInstance.set("firstName", "Cicero");});

		aBeanInstance.state(State.PREPARE);
		aMapInstance.state(State.PREPARE);
		assertEquals(State.PREPARE, aBeanInstance.state());
		assertEquals(State.PREPARE, aMapInstance.state());
		aBeanInstance.set("firstName", "Cicero");
		aMapInstance.set("firstName", "Cicero");
		
		assertEquals("Cicero", aBeanInstance.getFirstName());
		assertEquals("Cicero", aMapInstance.get("firstName"));


		aBeanInstance.state(State.STORED);
		aMapInstance.state(State.STORED);
		assertEquals(State.STORED, aBeanInstance.state());
		assertEquals(State.STORED, aMapInstance.state());

		assertThrows(Exception.class, () -> {aBeanInstance.set("firstName", "Cicero");});
		assertThrows(Exception.class, () -> {aMapInstance.set("firstName", "Cicero");});
		
		aBeanInstance.state(State.OUTDATED);
		aMapInstance.state(State.OUTDATED);
		assertEquals(State.OUTDATED, aBeanInstance.state());
		assertEquals(State.OUTDATED, aMapInstance.state());

		assertThrows(Exception.class, () -> {aBeanInstance.set("firstName", "Cicero");});
		assertThrows(Exception.class, () -> {aMapInstance.set("firstName", "Cicero");});
		

		aBeanInstance = entityCompanion.createInstance(12);
		aMapInstance = mapCompanion.createInstance(12);

		aBeanInstance.state(State.RECORD);
		aMapInstance.state(State.RECORD);
		assertEquals(State.RECORD, aBeanInstance.state());
		assertEquals(State.RECORD, aMapInstance.state());
		aBeanInstance.set("firstName", "Cicero");
		aMapInstance.set("firstName", "Cicero");
		
		assertEquals("Cicero", aBeanInstance.getFirstName());
		assertEquals("Cicero", aMapInstance.get("firstName"));
		
	}
	
}
