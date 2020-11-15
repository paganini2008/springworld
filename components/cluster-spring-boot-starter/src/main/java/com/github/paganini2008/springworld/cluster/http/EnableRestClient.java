package com.github.paganini2008.springworld.cluster.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * EnableRestClient
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ RestClientRegistrar.class })
public @interface EnableRestClient {

	String[] basePackages() default {};

	Class<?>[] include() default {};

}
