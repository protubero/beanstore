package de.protubero.beanstore.base;

public interface BeanChange<T extends AbstractPersistentObject> {

	public static enum ChangeType {
		Create,
		Update,
		Delete
	}
	
	ChangeType type();
	
	Long instanceId();
	
	T replacedInstance();

	T newInstance();

	BeanPropertyChange[] changes();

	BeanStoreEntity<T> entity();
	
	
}
