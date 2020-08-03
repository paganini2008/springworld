package com.github.paganini2008.springworld.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * SchedulerAutoConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@Import({ ServerModeSchedulerConfiguration.class, EmbeddedModeSchedulerConfiguration.class })
public class SchedulerAutoConfiguration {
}
