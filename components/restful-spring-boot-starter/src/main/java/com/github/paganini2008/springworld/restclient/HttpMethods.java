package com.github.paganini2008.springworld.restclient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * HttpMethods
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class HttpMethods {

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Get {

		String service();

		String contentType();

		int retries() default 0;

		int timeout() default -1;

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Post {

		String service();

		String produces();

		int retries() default 0;

		int timeout() default -1;

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Delete {

		String service();

		int retries() default 0;

		int timeout() default -1;

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Put {

		String service();

		int retries() default 0;

		int timeout() default -1;

	}

}
