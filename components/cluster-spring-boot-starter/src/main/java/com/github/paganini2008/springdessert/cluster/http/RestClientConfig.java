package com.github.paganini2008.springdessert.cluster.http;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterAware;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.LeaderRecoveryCallback;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastConfig;
import com.github.paganini2008.springdessert.cluster.utils.LoadBalancer;

/**
 * 
 * RestClientConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@ConditionalOnBean(ApplicationMulticastConfig.class)
public class RestClientConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public RegistryCenter applicationRegistryCenter() {
		return new ApplicationRegistryCenter();
	}

	@Bean
	public LoadBalancer applicationClusterLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public RoutingPolicy routingPolicy() {
		return new LoadBalanceRoutingPolicy();
	}

	@Bean
	public EnhancedRestTemplate enhancedRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
		return new EnhancedRestTemplate(clientHttpRequestFactory);
	}

	@Bean
	public RequestInterceptorContainer requestInterceptorContainer() {
		return new RequestInterceptorContainer();
	}

	@Bean
	public RetryTemplateFactory retryTemplateFactory() {
		return new RetryTemplateFactory();
	}

	@Bean
	public LoggingRetryListener loggingRetryListener() {
		return new LoggingRetryListener();
	}

	@ConditionalOnMissingBean
	@Bean
	public ThreadPoolTaskExecutor restClientThreadPool() {
		final int nThreads = Runtime.getRuntime().availableProcessors();
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(nThreads);
		taskExecutor.setMaxPoolSize(nThreads * 2);
		taskExecutor.setThreadFactory(new PooledThreadFactory("rest-client-threads-"));
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		return taskExecutor;
	}

	/**
	 * 
	 * LeaderHeartbeaterConfig
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	@EnableRestClient(include = { LeaderService.class })
	@Configuration
	@ConditionalOnProperty(name = "spring.application.cluster.leader.heartbeat.enabled", havingValue = "true")
	public static class LeaderHeartbeaterConfig {

		@Bean
		public LeaderHeartbeater leaderHeartbeater() {
			return new LeaderHeartbeater();
		}

		@Primary
		@Bean
		public LeaderRecoveryCallback retryableLeaderRecoveryCallback() {
			return new RetryableLeaderRecoveryCallback();
		}

	}

	/**
	 * 
	 * HttpClientConfig
	 *
	 * @author Fred Feng
	 * 
	 * @since 1.0
	 */
	@ConditionalOnMissingBean(ClientHttpRequestFactory.class)
	@Configuration
	public static class HttpClientConfig {

		@Value("${spring.application.cluster.httpclient.pool.maxTotal:200}")
		private int maxTotal;

		@Value("${spring.application.cluster.httpclient.retryCount:3}")
		private int retryCount;

		@Value("${spring.application.cluster.httpclient.connectionTimeout:60000}")
		private int connectionTimeout;

		@Bean
		public ClientHttpRequestFactory clientHttpRequestFactory() {
			return new HttpComponentsClientHttpRequestFactory(defaultHttpClient());
		}

		@Bean
		public HttpClient defaultHttpClient() {
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
			connectionManager.setMaxTotal(maxTotal);
			connectionManager.setDefaultMaxPerRoute(20);
			connectionManager.setValidateAfterInactivity(10000);
			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
					.setCircularRedirectsAllowed(false).setRedirectsEnabled(false).setSocketTimeout(connectionTimeout)
					.setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(connectionTimeout);
			HttpClientBuilder builder = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, true))
					.setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfigBuilder.build());
			return builder.build();
		}
	}

}
