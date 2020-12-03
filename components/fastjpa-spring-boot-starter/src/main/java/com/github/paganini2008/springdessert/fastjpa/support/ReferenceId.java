package com.github.paganini2008.springdessert.fastjpa.support;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * ReferenceId
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ReferenceId {

	Class<? extends InjectionHandler> using() default NewEntityInjectionHandler.class;

	String targetProperty();

}
