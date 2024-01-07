package de.protubero.beanstore.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.model.Employee;

public class StoreTest {

	@Test
	public void testImmutableStore() {
		List<ImmutableEntityStoreBase<?>> baseList = new ArrayList<>();
		ImmutableEntityStoreBase<Employee> base = new ImmutableEntityStoreBase<>();
		EntityCompanion<Employee> companion = new EntityCompanion<>(Employee.class);
		base.setCompanion(companion);
		base.setNextInstanceId(13);
		base.setObjectMap(new HashMap<>());
		baseList.add(base);
		
		ImmutableEntityStoreSet storeSet = new ImmutableEntityStoreSet(baseList.toArray(new ImmutableEntityStoreBase[baseList.size()]), 4);
		assertTrue(storeSet.isImmutable());
		var empStore = storeSet.store(Employee.class);
		assertSame(companion, empStore.companion());
		assertEquals(13, empStore.getAndIncreaseInstanceId());
		assertEquals(0, empStore.size());
		assertTrue(empStore.isEmpty());
		assertEquals(4, storeSet.version());
		
		assertFalse(storeSet.hasNoEntityStores());
		assertTrue(storeSet.hasNoData());
		assertSame(empStore, storeSet.store("employee"));
	
		var clonedSet = storeSet.internalCloneStoreSet();
		var clonedEmpStore = clonedSet.store(Employee.class);
		assertSame(companion, clonedEmpStore.companion());
		assertEquals(14, clonedEmpStore.getAndIncreaseInstanceId());
		assertEquals(0, clonedEmpStore.size());
		assertTrue(clonedEmpStore.isEmpty());
		assertEquals(5, clonedSet.version());
		
		assertFalse(clonedSet.hasNoEntityStores());
		assertTrue(clonedSet.hasNoData());
		assertSame(clonedEmpStore, clonedSet.store("employee"));
	}
	
	/*
	@Test
	public void testMutableStore() {
		List<Companion<?>> baseList = new ArrayList<>();
		EntityCompanion<Employee> companion = new EntityCompanion<>(Employee.class);
		baseList.add(companion);
		
		MutableEntityStoreSet storeSet = new MutableEntityStoreSet(baseList);
		assertTrue(storeSet.isImmutable());
		var empStore = storeSet.store(Employee.class);
		assertSame(companion, empStore.companion());
		assertEquals(13, empStore.getAndIncreaseInstanceId());
		assertEquals(0, empStore.size());
		assertTrue(empStore.isEmpty());
		assertEquals(4, storeSet.version());
		
		assertFalse(storeSet.hasNoEntityStores());
		assertTrue(storeSet.hasNoData());
		assertSame(empStore, storeSet.store("employee"));
	
		var clonedSet = storeSet.internalCloneStoreSet();
		var clonedEmpStore = clonedSet.store(Employee.class);
		assertSame(companion, clonedEmpStore.companion());
		assertEquals(14, clonedEmpStore.getAndIncreaseInstanceId());
		assertEquals(0, clonedEmpStore.size());
		assertTrue(clonedEmpStore.isEmpty());
		assertEquals(5, clonedSet.version());
		
		assertFalse(clonedSet.hasNoEntityStores());
		assertTrue(clonedSet.hasNoData());
		assertSame(clonedEmpStore, clonedSet.store("employee"));
	}
	*/
}
