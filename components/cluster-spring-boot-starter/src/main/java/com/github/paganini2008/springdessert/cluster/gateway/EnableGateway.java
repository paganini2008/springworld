package com.github.paganini2008.springdessert.cluster.gateway;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * EnableGateway
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ GatewayAutoConfiguration.class })
public @interface EnableGateway {
}
