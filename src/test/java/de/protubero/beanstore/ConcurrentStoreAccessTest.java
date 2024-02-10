package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.EntityStoreSnapshot;
import de.protubero.beanstore.model.Employee;

public class ConcurrentStoreAccessTest extends AbstractBeanStoreTest {

	long newEmployeeId;
	
	@TempDir
	File pFileDir;
	
	@Override
	protected File getFileDir() {
		return pFileDir;
	}
	
	@Test
	public void test()  {
		var store = addSampleData(createEmptyStore());

		EntityStoreSnapshot<Employee> employeeStore = store.snapshot().entity(Employee.class);
		
		// in the meanwhile ...
		// create a new instance
		var tx = store.transaction();
		var newEmployee = tx.create(Employee.class);
		newEmployee.setFirstName("John");
		newEmployee.setLastName("Walter");
		newEmployee.setAge(27);
		tx.execute();
		newEmployeeId = newEmployee.id().longValue();
		
		// update existing ones
		tx = store.transaction();
		var morlock = employeeStore.stream().filter(e -> e.getFirstName().equals("Ottmar")).findAny().get();
		tx.update(morlock).setAge(101);
		tx.execute();

		// delete some
		tx = store.transaction();
		for (var employee : employeeStore) {
			if (!employee.getLastName().equals("Walter")) {
				tx.delete(employee);
			}	
		}
		tx.execute();

		var originalList = employeeStore.stream().collect(Collectors.toList());
		checkList(originalList);
		

		var resultingList = store.snapshot().entity(Employee.class).stream().collect(Collectors.toList());
		assertEquals(3, resultingList.size());
		for (Employee remainingEmp : resultingList) {
			assertEquals("Walter", remainingEmp.getLastName());
			if (remainingEmp.getFirstName().equals("Ottmar")) {
				assertEquals(101, remainingEmp.getAge());
			}
		}
		
		
	}

	private void checkList(List<Employee> employeeList) {
		assertEquals(SAMPLE_DATA.length, employeeList.size());
		for (Employee emp : employeeList) {
			assertEqualsSampleData(emp);
		}
	}
	
}
