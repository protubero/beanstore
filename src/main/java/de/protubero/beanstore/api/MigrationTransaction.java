package de.protubero.beanstore.api;

/**
 * The kind of transaction which is used with migrations. <br>
 * Migration only operate on MapObjects. 
 *
 */
public interface MigrationTransaction extends BaseTransaction {

	BeanStoreState state();
	
}
