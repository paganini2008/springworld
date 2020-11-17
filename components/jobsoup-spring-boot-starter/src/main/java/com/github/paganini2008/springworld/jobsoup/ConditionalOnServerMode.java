package com.github.paganini2008.springworld.jobsoup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

import com.github.paganini2008.springworld.jobsoup.server.ServerMode;

/**
 * 
 * ConditionalOnServerMode
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnServerModeCondition.class)
public @interface ConditionalOnServerMode {

	ServerMode value() default ServerMode.CONSUMER;
}
