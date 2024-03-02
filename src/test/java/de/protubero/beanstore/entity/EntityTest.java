package de.protubero.beanstore.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.model.Employee;

public class EntityTest {

	@Test
	public void testCompanionTypeFlags() {
		EntityCompanion<Employee> entityCompanion =  CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		MapObjectCompanion mapCompanion = CompanionRegistry.getOrCreateMapCompanion("employee");
		
		assertTrue(entityCompanion.isBean());	
		assertFalse(entityCompanion.isMapCompanion());
		
		assertTrue(mapCompanion.isMapCompanion());	
		assertFalse(mapCompanion.isBean());
		
	}	
	
	@Test
	public void testEntityClassAndAlias() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		MapObjectCompanion mapCompanion = CompanionRegistry.getOrCreateMapCompanion("employee");

		assertEquals("employee", entityCompanion.alias());
		assertEquals("employee", mapCompanion.alias());
		
		assertEquals(Employee.class, entityCompanion.entityClass());
		assertThrows(UnsupportedOperationException.class, () -> {mapCompanion.entityClass();});
		
		assertTrue(Employee.class.isAssignableFrom(entityCompanion.beanClass()));
	}	
	
	@Test
	public void createInstanceWithoutId() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		MapObjectCompanion mapCompanion = CompanionRegistry.getOrCreateMapCompanion("employee");
		
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
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		final Employee aBeanInstance = entityCompanion.createInstance();
		aBeanInstance.state(State.PREPARE);
		
		aBeanInstance.set("firstName", "John");
		assertEquals("John", aBeanInstance.get("firstName"));
		assertThrows(Exception.class, () -> {aBeanInstance.set("abc", 1);});
	}
	
	@Test
	public void gettingUnknownPropThrowsException() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		final Employee aBeanInstance = entityCompanion.createInstance();
		aBeanInstance.state(State.PREPARE);
		
		aBeanInstance.set("firstName", "John");
		assertEquals("John", aBeanInstance.get("firstName"));
		assertThrows(Exception.class, () -> {aBeanInstance.get("abc");});
	}

	@Test
	public void createInstanceWithId() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		MapObjectCompanion mapCompanion = CompanionRegistry.getOrCreateMapCompanion("employee");
		
		final Employee aBeanInstance = entityCompanion.createInstance(12);
		assertEquals(12, aBeanInstance.id());
		
		final MapObject aMapInstance = mapCompanion.createInstance(12);
		assertEquals(12, aMapInstance.id());
	}
	
	
	@Test
	public void propChangesAreOnlyAllowedInCertainStates() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		MapObjectCompanion mapCompanion = CompanionRegistry.getOrCreateMapCompanion("employee");
		
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
		

		final var tBeanInstance = entityCompanion.createInstance(12);
		final var tMapInstance = mapCompanion.createInstance(12);

		tBeanInstance.state(State.RECORD);
		tMapInstance.state(State.RECORD);
		assertEquals(State.RECORD, tBeanInstance.state());
		assertEquals(State.RECORD, tMapInstance.state());
		tBeanInstance.set("firstName", "Cicero");
		tMapInstance.set("firstName", "Cicero");
		
		assertEquals("Cicero", tBeanInstance.getFirstName());
		assertEquals("Cicero", tMapInstance.get("firstName"));
		
		tBeanInstance.state(State.RECORDED);
		tMapInstance.state(State.RECORDED);
		assertEquals(State.RECORDED, tBeanInstance.state());
		assertEquals(State.RECORDED, tMapInstance.state());

		assertThrows(Exception.class, () -> {tBeanInstance.set("firstName", "Cicero");});
		assertThrows(Exception.class, () -> {tMapInstance.set("firstName", "Cicero");});
	}

	
	@Test
	public void unmanagedState() {
		var employee = new Employee();
		assertEquals(State.UNMANAGED, employee.state());
		
		assertEquals("employee", employee.alias());
		
		employee.setFirstName("Cicero");
		
		assertEquals("Cicero", employee.getFirstName());
		assertNotNull(employee.companion());
		assertNull(employee.changes());
		assertThrows(Exception.class, () -> {employee.state(State.RECORD);});
		assertNull(employee.id());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void invalidEntityClasses() {
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithoutNoArgsConstructor.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithoutReadMethod.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithoutWriteMethod.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithDefaultValue.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithoutAnnotation.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion(EmployeeWithInvalidRef.class));
		assertThrows(Exception.class, () -> CompanionRegistry.getOrCreateEntityCompanion((Class) EmployeeWithoutInheritence.class));
		
	}
	
	@Test
	public void mapishEntity() {
		EntityCompanion<Employee> entityCompanion = CompanionRegistry.getOrCreateEntityCompanion(Employee.class);
		final Employee emp = entityCompanion.createInstance(1);
		
		var keySet = emp.keySet();
		assertEquals(4, keySet.size());
		keySet.containsAll(List.of("age", "firstName", "lastName", "employeeNumber"));
		
		assertTrue(emp.containsKey("age"));
		assertFalse(emp.containsKey("abc"));
		
		assertThrows(UnsupportedOperationException.class, () -> {emp.containsValue(1);});
		
		var entryMap = new HashMap<>();
		for (var entry : emp.entrySet()) {
			entryMap.put(entry.getKey(), entry.getValue());
		}
		
		var entryMapKeySet = entryMap.keySet();
		assertEquals(4, entryMapKeySet.size());
		entryMapKeySet.containsAll(List.of("age", "firstName", "lastName", "employeeNumber"));
		
		assertTrue(entryMap.containsKey("age"));
		assertFalse(entryMap.containsKey("abc"));
	}
	
}
