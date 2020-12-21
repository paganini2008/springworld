package com.github.paganini2008.springdessert.cluster.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.LoadBalancer;

/**
 * 
 * RestClientConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
public class RestClientConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public LoadBalancer applicationClusterLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public RoutingAllocator routingAllocator() {
		return new LoadBalanceRoutingAllocator();
	}

	@ConditionalOnMissingBean
	@Bean
	public RestClientPerformer restClientPerformer(ClientHttpRequestFactory clientHttpRequestFactory) {
		return new DefaultRestClientPerformer(clientHttpRequestFactory, CharsetUtils.UTF_8);
	}

	@Bean
	public RetryTemplateFactory retryTemplateFactory() {
		return new RetryTemplateFactory();
	}

	@Bean
	public RequestInterceptorContainer requestInterceptorContainer() {
		return new RequestInterceptorContainer();
	}

	@Bean
	public RequestTemplate genericRequestTemplate(RoutingAllocator routingAllocator, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, ThreadPoolTaskExecutor taskExecutor,
			RequestInterceptorContainer requestInterceptorContainer, @Qualifier("requestStatistic") StatisticIndicator statisticIndicator) {
		return new RequestTemplate(routingAllocator, restClientPerformer, retryTemplateFactory, taskExecutor, requestInterceptorContainer,
				statisticIndicator);
	}

	@Bean
	public LoggingRetryListener loggingRetryListener() {
		return new LoggingRetryListener();
	}

	@Bean("requestStatistic")
	public StatisticIndicator requestStatistic() {
		return new RequestStatisticIndicator();
	}

	@Bean("responseStatistic")
	public StatisticIndicator responseStatistic() {
		return new ResponseStatisticIndicator();
	}

	/**
	 * 
	 * HttpClientConfig
	 *
	 * @author Jimmy Hoff
	 * 
	 * @since 1.0
	 */
	@ConditionalOnMissingBean(ClientHttpRequestFactory.class)
	@Configuration
	public static class HttpClientConfig {

		@Value("${spring.application.cluster.httpclient.pool.maxTotal:200}")
		private int maxTotal;

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
			HttpClientBuilder builder = HttpClients.custom().disableAutomaticRetries().setConnectionManager(connectionManager)
					.setDefaultRequestConfig(requestConfigBuilder.build());
			return builder.build();
		}
	}

}
