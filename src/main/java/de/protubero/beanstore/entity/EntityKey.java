package de.protubero.beanstore.entity;

import java.util.Objects;
import java.util.Optional;

public interface EntityKey<T extends AbstractEntity> extends PersistentObjectKey {

	Class<T> entityClass();
	
	public static <T extends AbstractEntity> EntityKey<T> of(Class<T> entityClass, long id) {
		Objects.requireNonNull(entityClass);
		Objects.requireNonNull(id);

		Optional<EntityCompanion<T>> companion = CompanionRegistry.getEntityCompanionByClass(entityClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class " + entityClass);
		}
		String alias = companion.get().alias();
		
		
		return new EntityKey<T>() {

			@Override
			public String alias() {
				return alias;
			}

			@Override
			public Long id() {
				return id;
			}

			@Override
			public Class<T> entityClass() {
				return entityClass;
			}
			
		};
	}

	static <T extends AbstractEntity> EntityKey<?> of(EntityCompanion<T> companion, Long id) {
		return new EntityKey<T>() {

			@Override
			public String alias() {
				return companion.alias();
			}

			@Override
			public Long id() {
				return id;
			}

			@Override
			public Class<T> entityClass() {
				return companion.entityClass();
			}
			
		};
	}	
	
}
