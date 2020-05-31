package com.github.paganini2008.springworld.redisplus;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.redisplus.messager.RedisMessagerConfig;

/**
 * 
 * RedisPlusAutoConfiguration
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ RedisMessagerConfig.class })
public class RedisPlusAutoConfiguration {

}
