package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.reditools.common.IdGenerator;
import com.github.paganini2008.springdessert.reditools.common.TimestampIdGenerator;
import com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger;
import com.github.paganini2008.transport.HashPartitioner;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.RoundRobinPartitioner;

/**
 * 
 * WebCrawlerAutoConfiguration
 *
 * @author Fred Feng
 * @since 1.0
 */
@Configuration
public class WebCrawlerAutoConfiguration {

	@Value("${spring.application.name}")
	private String applicationName;

	@Bean
	public CrawlerLauncher crawlerLauncher() {
		return new CrawlerLauncher();
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
	public PageExtractor pageExtractor() {
		return new HtmlUnitPageExtractor();
	}

	@ConditionalOnMissingBean
	@Bean
	public IdGenerator globalIdGenerator(RedisConnectionFactory connectionFactory) {
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

}
