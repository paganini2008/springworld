package com.github.paganini2008.springdessert.cached;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * ApplicationClusterCacheAutoConfiguration
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Configuration
@Import(ApplicationClusterCacheConfig.class)
public class ApplicationClusterCacheAutoConfiguration {
}
