package com.github.paganini2008.springdessert.xtransport;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * TransportAutoConfiguration
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
@Import({ TransportServerConfiguration.class, ApplicationTransportController.class, BenchmarkController.class })
@Configuration
public class TransportAutoConfiguration {
}
