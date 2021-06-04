package de.protubero.beanstore.base.entity;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this Annotation to associate a Java Bean entity class with an <i>alias</i>.  
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Entity {

	String alias();
}
