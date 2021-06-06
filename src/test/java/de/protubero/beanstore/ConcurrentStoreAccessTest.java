package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStoreReadAccess;
import de.protubero.beanstore.api.EntityReadAccess;
import de.protubero.beanstore.model.Employee;

public class ConcurrentStoreAccessTest extends AbstractBeanStoreTest {

	long newEmployeeId;
	
	@Test
	public void test() {
		var store = addSampleData(createEmptyStore());

		EntityReadAccess<Employee> employeeStore = store.read().entity(Employee.class);
		BeanStoreReadAccess snapshot = store.read().snapshot();
		Stream<Employee> stream = employeeStore.stream();
		
		// in the meanwhile ...
		// create a new instance
		store.locked(tf -> {
			var tx = tf.transaction();
			var newEmployee = tx.create(Employee.class);
			newEmployee.setFirstName("John");
			newEmployee.setLastName("Walter");
			newEmployee.setAge(27);
			tx.execute();
			newEmployeeId = newEmployee.id().longValue();
		});
		
		// update existing ones
		store.locked(tf -> {
			var tx = tf.transaction();
			var morlock = employeeStore.stream().filter(e -> e.getFirstName().equals("Ottmar")).findAny().get();
			tx.update(morlock).setAge(101);
			tx.execute();
		});

		// delete some
		store.locked(tf -> {
			var tx = tf.transaction();
			for (var employee : employeeStore) {
				if (!employee.getLastName().equals("Walter")) {
					tx.delete(employee);
				}	
			}
			tx.execute();
		});

		var originalList = stream.collect(Collectors.toList());
		checkList(originalList);
		
		
		var resultingList = employeeStore.stream().collect(Collectors.toList());
		assertEquals(3, resultingList.size());
		for (Employee remainingEmp : resultingList) {
			assertEquals("Walter", remainingEmp.getLastName());
			if (remainingEmp.getFirstName().equals("Ottmar")) {
				assertEquals(101, remainingEmp.getAge());
			}
		}
		
		var snapshotList = snapshot.entity(Employee.class).stream().collect(Collectors.toList());
		checkList(snapshotList);
		
	}

	private void checkList(List<Employee> employeeList) {
		assertEquals(SAMPLE_DATA.length, employeeList.size());
		for (Employee emp : employeeList) {
			assertEqualsSampleData(emp);
		}
	}
	
}
