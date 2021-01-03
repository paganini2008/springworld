package com.github.paganini2008.springdessert.logtracker;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.github.paganini2008.springdessert.logtracker.es.LogEntryService;
import com.github.paganini2008.springdessert.logtracker.ui.LogTraceController;

import lombok.Setter;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * LogTrackerAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@EnableElasticsearchRepositories("com.github.paganini2008.springdessert.logtracker.es")
@Import({ LogTraceController.class })
@Configuration
public class LogTrackerAutoConfiguration {

	@Bean
	public LogHandler logHandler() {
		return new LogHandler();
	}

	@Bean
	public LogEntryService logEntryService() {
		return new LogEntryService();
	}

	@Setter
	@Configuration
	@ConfigurationProperties(prefix = "spring.application.cluster.logtracker.redis")
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
			jedisPoolConfig.setMaxTotal(100);
			jedisPoolConfig.setMaxWaitMillis(-1);
			jedisPoolConfig.setTestWhileIdle(true);
			return jedisPoolConfig;
		}

	}

}
