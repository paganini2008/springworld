package com.github.paganini2008.springdessert.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * ApplicationPropertiesAutoConfigutation
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@ConditionalOnWebApplication
@Configuration
@Import({ ApplicationPropertiesConfig.class, ApplicationPropertiesKeeperConfig.class, ApplicationPropertiesController.class})
public class ApplicationPropertiesAutoConfigutation {
}
