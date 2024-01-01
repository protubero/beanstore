package de.protubero.beanstore.plugins.validate;

import java.util.Set;

import de.protubero.beanstore.entity.AbstractEntity;
import jakarta.validation.ConstraintViolation;

public class BeanValidationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -547680883490182008L;

	private Set<ConstraintViolation<AbstractEntity>> violations;
	

	public BeanValidationException(Set<ConstraintViolation<AbstractEntity>> violations) {
		this.violations = violations;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		violations.forEach(v -> {
			sb.append(v.getMessage());
		});
		return sb.toString();
	}

	public Set<ConstraintViolation<AbstractEntity>> getViolations() {
		return violations;
	}

	
}
