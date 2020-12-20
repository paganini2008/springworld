package com.github.paganini2008.springdessert.cluster.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.HashCache;
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
	
	@Bean
	public RouterManager routerManager() {
		return new RouterManager();
	}

	@ConditionalOnMissingBean
	@Bean
	public EmbeddedServer embeddedServer() {
		return new NettyEmbeddedServer();
	}

	@ConditionalOnMissingBean
	@Bean
	public HttpRequestDispatcher httpRequestDispatcher() {
		return new AsynchronousHttpRequestDispatcher();
	}

	@ConditionalOnMissingBean
	@Bean
	public Cache remoteCache() {
		return new HashCache().expiredCache(3);
	}

	@ConditionalOnMissingBean
	@Bean
	public RouterCustomizer routerCustomizer() {
		return new DefaultRouterCustomizer();
	}

}
