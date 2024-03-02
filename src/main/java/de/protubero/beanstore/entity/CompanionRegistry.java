package de.protubero.beanstore.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CompanionRegistry {

	private static Map<Class<?>, EntityCompanion<?>> entityCompanionByClassMap = new HashMap<>();
	private static Map<String, EntityCompanion<?>> entityCompanionByAliasMap = new HashMap<>();
	
	private static Map<String, MapObjectCompanion> mapCompanionByAliasMap = new HashMap<>();
	
	public static synchronized MapObjectCompanion getOrCreateMapCompanion(String alias) {
		MapObjectCompanion result = mapCompanionByAliasMap.get(Objects.requireNonNull(alias));
		if (result != null) {
			return (MapObjectCompanion) result;
		} else {
			result = new MapObjectCompanion(alias);
			mapCompanionByAliasMap.put(alias, result);
			return result;
		} 
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractEntity> EntityCompanion<T> getOrCreateEntityCompanion(Class<T> originalBeanClass) {
		EntityCompanion<T> result = (EntityCompanion<T>) entityCompanionByClassMap.get(Objects.requireNonNull(originalBeanClass));
		if (result == null) {
			result = new EntityCompanion<T>(originalBeanClass);
			
			if (entityCompanionByAliasMap.containsKey(result.alias())) {
				throw new RuntimeException("Duplicat data bean alias " + result.alias());
			}
			
			entityCompanionByClassMap.put(originalBeanClass, result);
			entityCompanionByAliasMap.put(result.alias(), result);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractEntity> Optional<EntityCompanion<T>> getEntityCompanionByAlias(String alias) {
		return Optional.ofNullable((EntityCompanion<T>) entityCompanionByAliasMap.get(alias));
	}

	public static Optional<MapObjectCompanion>getMapCompanionByAlias(String alias) {
		return Optional.ofNullable(mapCompanionByAliasMap.get(Objects.requireNonNull(alias)));
	}
	
}
