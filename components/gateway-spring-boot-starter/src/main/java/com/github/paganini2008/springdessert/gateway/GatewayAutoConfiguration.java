package com.github.paganini2008.springdessert.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * GatewayAutoConfiguration
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Configuration
public class GatewayAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public EmbeddedHttpServer embeddedHttpServer() {
		return new NettyHttpServer();
	}

	@ConditionalOnMissingBean
	@Bean
	public HttpRequestDispatcher httpRequestDispatcher() {
		return new SynchronousHttpRequestDispatcher();
	}

}
