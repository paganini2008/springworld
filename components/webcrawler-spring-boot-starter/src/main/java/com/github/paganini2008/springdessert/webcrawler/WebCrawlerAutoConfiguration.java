package com.github.paganini2008.springdessert.webcrawler;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.springdessert.reditools.common.IdGenerator;
import com.github.paganini2008.springdessert.reditools.common.TimestampIdGenerator;
import com.github.paganini2008.springdessert.webcrawler.es.IndexedResourceService;
import com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger;
import com.github.paganini2008.xtransport.HashPartitioner;
import com.github.paganini2008.xtransport.Partitioner;
import com.github.paganini2008.xtransport.RoundRobinPartitioner;

/**
 * 
 * WebCrawlerAutoConfiguration
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@EnableElasticsearchRepositories("com.github.paganini2008.springdessert.webcrawler.es")
@Configuration
public class WebCrawlerAutoConfiguration {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public CrawlerLauncher crawlerLauncher() {
		return new CrawlerLauncher();
	}

	@Bean
	public PathFilterFactory pathFilterFactory(StringRedisTemplate redisTemplate) {
		return new BloomFilterPathFilterFactory(redisTemplate);
	}

	@Bean
	public CrawlerHandler crawlerHandler() {
		return new CrawlerHandler();
	}

	@Primary
	@Bean
	public Partitioner partitioner() {
		return new HashPartitioner("catalogId,refer,path,version".split(","));
	}

	@Bean
	public Partitioner secondaryPartitioner() {
		return new RoundRobinPartitioner();
	}

	@ConditionalOnMissingBean
	@Bean
	public ResourceManager resourceService() {
		return new JdbcResourceManger();
	}

	@ConditionalOnMissingBean
	@Bean
	public PageExtractor pageExtractor(ClientHttpRequestFactory clientHttpRequestFactory) {
		return new HttpClientPageExtractor(new RestTemplate(clientHttpRequestFactory));
	}

	@ConditionalOnMissingBean
	@Bean
	public IdGenerator timestampIdGenerator(RedisConnectionFactory redisConnectionFactory) {
		final String keyPrefix = String.format("spring:application:cluster:%s:id:", clusterName);
		return new TimestampIdGenerator(keyPrefix, redisConnectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public ConditionalTermination conditionalTermination(CrawlerSummary crawlerSummary, RedisConnectionFactory redisConnectionFactory) {
		return new CountingConditionalTermination(crawlerSummary, 20, TimeUnit.MINUTES, redisConnectionFactory, 100000);
	}

	@ConditionalOnMissingBean
	@Bean
	public PathAcceptor pathAcceptor() {
		return new DefaultPathAcceptor();
	}

	@Bean
	public IndexedResourceService indexedResourceService() {
		return new IndexedResourceService();
	}

	@Bean
	public CrawlerSummary crawlerSummary(RedisConnectionFactory redisConnectionFactory) {
		return new CrawlerSummary(clusterName, redisConnectionFactory);
	}

}
