package com.github.paganini2008.springworld.jobswarm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.jobswarm.server.ServerModeSchedulerConfiguration;
import com.github.paganini2008.springworld.jobswarm.ui.UIModeConfiguration;

/**
 * 
 * JobSwarmAutoConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@ConditionalOnWebApplication
@Configuration
@Import({ EmbeddedModeSchedulerConfiguration.class, ServerModeSchedulerConfiguration.class, UIModeConfiguration.class })
public class JobSwarmAutoConfiguration {
}
