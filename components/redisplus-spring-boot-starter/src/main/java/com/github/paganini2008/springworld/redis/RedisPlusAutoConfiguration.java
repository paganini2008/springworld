package com.github.paganini2008.springworld.redis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * RedisPlusAutoConfiguration
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
@Import({ RedisPubSubConfig.class })
public class RedisPlusAutoConfiguration {

}
