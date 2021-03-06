package com.github.paganini2008.springworld.support;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Comment
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
@Target({ TYPE, FIELD })
@Retention(RUNTIME)
public @interface Comment {
	String value();
}
