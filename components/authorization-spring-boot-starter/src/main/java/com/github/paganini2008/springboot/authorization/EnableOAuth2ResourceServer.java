package com.github.paganini2008.springboot.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * EnableOAuth2ResourceServer
 * 
 * @author Jimmy Hoff
 * 
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OAuth2ResourceServerConfig.class)
public @interface EnableOAuth2ResourceServer {

}
