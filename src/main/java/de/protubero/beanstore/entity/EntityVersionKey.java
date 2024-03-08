package de.protubero.beanstore.entity;

import java.util.Objects;
import java.util.Optional;

public interface EntityVersionKey<T extends AbstractEntity> extends EntityKey<T>, PersistentObjectVersionKey {

	@Override
	int version();
	
	public static <T extends AbstractEntity> EntityKey<T> of(Class<T> entityClass, long id, int version) {
		Objects.requireNonNull(entityClass);

		Optional<EntityCompanion<T>> companion = CompanionRegistry.getEntityCompanionByClass(entityClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class " + entityClass);
		}
		String alias = companion.get().alias();
		
		
		return new EntityVersionKey<T>() {

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

			@Override
			public int version() {
				return version;
			}
			
		};
	}

	static <T extends AbstractEntity> EntityKey<?> of(EntityCompanion<T> companion, long id, int version) {
		Objects.requireNonNull(companion);
		
		return new EntityVersionKey<T>() {

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

			@Override
			public int version() {
				return version;
			}
			
		};
	}	
	
}
