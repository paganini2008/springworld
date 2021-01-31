package com.github.paganini2008.springdessert.jellyfish;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.github.paganini2008.springdessert.jellyfish.log.LogEntrySearchService;
import com.github.paganini2008.springdessert.jellyfish.log.LogEntryService;
import com.github.paganini2008.springdessert.jellyfish.log.Slf4jHandler;
import com.github.paganini2008.springdessert.jellyfish.stat.BulkStatisticHandler;
import com.github.paganini2008.springdessert.jellyfish.stat.DefaultMetricsCollectorCustomizer;
import com.github.paganini2008.springdessert.jellyfish.stat.MetricsCollectorCustomizer;
import com.github.paganini2008.springdessert.jellyfish.stat.RealtimeStatisticHandler;
import com.github.paganini2008.springdessert.jellyfish.stat.TransientStatisticSynchronizer;
import com.github.paganini2008.springdessert.reditools.common.IdGenerator;
import com.github.paganini2008.springdessert.reditools.common.TimestampIdGenerator;
import com.github.paganini2008.xtransport.HashPartitioner;
import com.github.paganini2008.xtransport.MultipleSelectionPartitioner;

import lombok.Setter;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * JellyfishAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@EnableElasticsearchRepositories("com.github.paganini2008.springdessert.jellyfish.log")
@Configuration(proxyBeanMethods = false)
public class JellyfishAutoConfiguration {

	private static final String keyPattern = "spring:application:cluster:jellyfish:%s:id";

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public Slf4jHandler slf4jHandler() {
		return new Slf4jHandler();
	}

	@Bean
	public RealtimeStatisticHandler realtimeStatisticalHandler() {
		return new RealtimeStatisticHandler();
	}

	@Bean
	public BulkStatisticHandler bulkStatisticalHandler() {
		return new BulkStatisticHandler();
	}

	@Autowired
	public void addHashPartitioner(MultipleSelectionPartitioner partitioner) {
		final String[] fieldNames = { "clusterName", "applicationName", "host", "path" };
		HashPartitioner hashPartitioner = new HashPartitioner(fieldNames);
		partitioner.addPartitioner("hash", hashPartitioner);
	}

	@Bean
	public TransientStatisticSynchronizer transientStatisticSynchronizer() {
		return new TransientStatisticSynchronizer();
	}

	@ConditionalOnMissingBean
	@Bean
	public MetricsCollectorCustomizer metricsCollectorCustomizer() {
		return new DefaultMetricsCollectorCustomizer();
	}

	@Bean
	public LogEntryService logEntryService() {
		return new LogEntryService();
	}

	@Bean
	public LogEntrySearchService logEntrySearchService() {
		return new LogEntrySearchService();
	}

	@ConditionalOnMissingBean(name = "logIdGenerator")
	@Bean
	public IdGenerator logIdGenerator(RedisConnectionFactory redisConnectionFactory) {
		final String keyPrefix = String.format(keyPattern, clusterName);
		return new TimestampIdGenerator(keyPrefix, redisConnectionFactory);
	}

	@Setter
	@Configuration
	@ConfigurationProperties(prefix = "spring.redis")
	public class RedisConfig {

		private String host = "localhost";
		private String password;
		private int port = 6379;
		private int dbIndex = 0;

		@ConditionalOnMissingBean
		@Bean
		public RedisConnectionFactory redisConnectionFactory() {
			RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
			redisStandaloneConfiguration.setHostName(host);
			redisStandaloneConfiguration.setPort(port);
			redisStandaloneConfiguration.setDatabase(dbIndex);
			redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
			JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
			jedisClientConfiguration.connectTimeout(Duration.ofMillis(60000)).readTimeout(Duration.ofMillis(60000)).usePooling()
					.poolConfig(jedisPoolConfig());
			JedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
			return factory;
		}

		@ConditionalOnMissingBean
		@Bean
		public JedisPoolConfig jedisPoolConfig() {
			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
			jedisPoolConfig.setMinIdle(1);
			jedisPoolConfig.setMaxIdle(10);
			jedisPoolConfig.setMaxTotal(200);
			jedisPoolConfig.setMaxWaitMillis(-1);
			jedisPoolConfig.setTestWhileIdle(true);
			return jedisPoolConfig;
		}

	}

}
