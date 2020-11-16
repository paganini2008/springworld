package com.github.paganini2008.springworld.reditools.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.reditools.BeanNames;

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
	public TtlKeeper ttlKeeper(@Qualifier(BeanNames.REDIS_TEMPLATE) RedisTemplate<String, Object> redisTemplate) {
		return new TtlKeeper(redisTemplate);
	}

}
