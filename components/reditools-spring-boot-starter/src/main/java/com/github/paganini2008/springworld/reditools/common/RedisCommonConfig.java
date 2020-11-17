package com.github.paganini2008.springworld.reditools.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * RedisCommonConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
public class RedisCommonConfig {

	@Bean
	public RedisKeepAliveResolver redisKeepAliveResolver() {
		return new RedisKeepAliveResolver();
	}

	@Bean
	public TtlKeeper ttlKeeper() {
		return new TtlKeeper();
	}

}
