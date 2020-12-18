package com.github.paganini2008.springdessert.cluster.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastConfig;

/**
 * 
 * GatewayAutoConfiguration
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@ConditionalOnBean(ApplicationMulticastConfig.class)
@Configuration
public class GatewayAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public EmbeddedServer embeddedServer() {
		return new NettyHttpServer();
	}

	@ConditionalOnMissingBean
	@Bean
	public HttpRequestDispatcher httpRequestDispatcher() {
		return new AsynchronousHttpRequestDispatcher();
	}

}
