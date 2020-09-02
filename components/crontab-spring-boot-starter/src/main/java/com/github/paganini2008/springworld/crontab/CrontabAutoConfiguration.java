package com.github.paganini2008.springworld.crontab;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.crontab.server.ServerModeSchedulerConfiguration;

/**
 * 
 * CrontabAutoConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@Import({ ServerModeSchedulerConfiguration.class, EmbeddedModeSchedulerConfiguration.class })
public class CrontabAutoConfiguration {
}
