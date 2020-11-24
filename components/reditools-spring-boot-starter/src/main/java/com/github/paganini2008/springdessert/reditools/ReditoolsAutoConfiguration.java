package com.github.paganini2008.springdessert.reditools;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springdessert.reditools.common.RedisCommonConfig;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageConfig;

/**
 * 
 * ReditoolsAutoConfiguration
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ RedisMessageConfig.class, RedisCommonConfig.class })
public class ReditoolsAutoConfiguration {
}
