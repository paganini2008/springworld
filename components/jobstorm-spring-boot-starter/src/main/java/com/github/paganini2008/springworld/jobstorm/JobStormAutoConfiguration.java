package com.github.paganini2008.springworld.jobstorm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.jobstorm.server.ServerModeConfiguration;
import com.github.paganini2008.springworld.jobstorm.ui.UIModeConfiguration;

/**
 * 
 * JobStormAutoConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@ConditionalOnWebApplication
@Configuration
@Import({ EmbeddedModeConfiguration.class, ServerModeConfiguration.class, UIModeConfiguration.class })
public class JobStormAutoConfiguration {
}
