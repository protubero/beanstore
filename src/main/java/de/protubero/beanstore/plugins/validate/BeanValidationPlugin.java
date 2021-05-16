package de.protubero.beanstore.plugins.validate;

import java.util.Set;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.StoreSnapshot;
import de.protubero.beanstore.base.BeanChange.ChangeType;
import de.protubero.beanstore.init.BeanStore;
import de.protubero.beanstore.init.BeanStorePlugin;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class BeanValidationPlugin implements BeanStorePlugin {

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();	

	@Override
	public void validate(AbstractPersistentObject apo) {
		if (apo.entity().isBean()) {
			doValidate((AbstractEntity) apo);
		}	
	}
	
	@Override
	public void onEndCreate(BeanStore beanStore, StoreSnapshot snapshot) {
		// verify newly created and updated beans
		beanStore.verifyInstance(change -> {
			if (change.entity().isBean()) {
				if (change.type() == ChangeType.Create || change.type() == ChangeType.Update) {
					doValidate((AbstractEntity) change.newInstance());
				}
			}
		});
	}
	
	private void doValidate(AbstractEntity instance) {
		Set<ConstraintViolation<AbstractEntity>> violations = validator.validate(instance);
		if (violations != null && violations.size() > 0) {
			throw new BeanValidationException(violations);
		}
	}

}
