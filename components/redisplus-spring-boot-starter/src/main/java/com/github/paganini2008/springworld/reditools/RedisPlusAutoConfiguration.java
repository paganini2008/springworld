package com.github.paganini2008.springworld.reditools;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.reditools.messager.RedisMessageConfig;

/**
 * 
 * RedisPlusAutoConfiguration
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ RedisMessageConfig.class })
public class RedisPlusAutoConfiguration {
}
