package de.protubero.beanstore.plugins.validate;

import java.util.Set;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.tx.InstanceEventType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class BeanValidationPlugin implements BeanStorePlugin {

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();	
	
	@Override
	public void onEndCreate(BeanStore beanStore) {
		// check all loaded instances
		beanStore.snapshot().forEach(es -> {
			if (es.meta().isBean()) {
				es.stream().forEach(apo -> {
					doValidate((AbstractEntity) apo);
				});
			}	
		});;
		
		// verify newly created and updated beans
		beanStore.callbacks().verifyInstance(change -> {
			if (change.entity().isBean()) {
				if (change.type() == InstanceEventType.Create || change.type() == InstanceEventType.Update) {
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
