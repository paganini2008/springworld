package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

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
 * @author Fred Feng
 * @since 1.0
 */
@EnableElasticsearchRepositories("com.github.paganini2008.springdessert.webcrawler.es")
@Configuration
public class WebCrawlerAutoConfiguration {

	@Value("${spring.application.name}")
	private String applicationName;

	@Bean
	public CrawlerLauncher crawlerLauncher() {
		return new CrawlerLauncher();
	}

	@Bean
	public PathFilterFactory pathFilterFactory(StringRedisTemplate redisTemplate) {
		return new BloomFilterPathFilterFactory(redisTemplate);
	}

	@Primary
	@Bean
	public Partitioner partitioner() {
		return new HashPartitioner("catalogId,refer,path,version".split(","));
	}

	@Bean("secondaryPartitioner")
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
	public PageExtractor pageExtractor() {
		return new HtmlUnitPageExtractor();
	}

	@ConditionalOnMissingBean
	@Bean
	public IdGenerator timestampIdGenerator(RedisConnectionFactory connectionFactory) {
		final String keyPrefix = "id:webcrawler:" + applicationName + ":";
		return new TimestampIdGenerator(keyPrefix, connectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public FinishableCondition countLimitedCondition(RedisConnectionFactory connectionFactory,
			@Value("${webcrawler.crawler.maxFetchSize:10000}") int maxFetchSize) {
		final String keyPrefix = "counter:webcrawler:" + applicationName + ":";
		return new CountLimitedCondition(keyPrefix, connectionFactory, maxFetchSize);
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

}
