package com.github.paganini2008.springworld.cache;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * ApplicationClusterCacheAutoConfiguration
 *
 * @author Fred Feng
 * @since 1.0
 */
@Configuration
@Import(ApplicationClusterCacheConfig.class)
public class ApplicationClusterCacheAutoConfiguration {
}
