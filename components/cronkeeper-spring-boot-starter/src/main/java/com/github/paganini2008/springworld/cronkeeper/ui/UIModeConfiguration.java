package com.github.paganini2008.springworld.cronkeeper.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.github.paganini2008.springworld.cronkeeper.JdbcJobManager;
import com.github.paganini2008.springworld.cronkeeper.JobAdmin;
import com.github.paganini2008.springworld.cronkeeper.JobIdCache;
import com.github.paganini2008.springworld.cronkeeper.JobManager;
import com.github.paganini2008.springworld.cronkeeper.LifecycleListenerContainer;
import com.github.paganini2008.springworld.cronkeeper.server.ClusterRegistry;
import com.github.paganini2008.springworld.cronkeeper.server.ClusterRestTemplate;
import com.github.paganini2008.springworld.cronkeeper.server.ServerModeJobAdmin;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * UIModeConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.deployMode", havingValue = "ui")
@Configuration
public class UIModeConfiguration {

	public UIModeConfiguration() {
		log.info("<<<                                                  >>>");
		log.info("<<<                Crontab v2.0-RC4                  >>>");
		log.info("<<<              Current Job Deploy Mode             >>>");
		log.info("<<<                 [UI Mode]                        >>>");
		log.info("<<<                                                  >>>");
	}

	@Bean
	public ClusterRegistry clusterRegistry() {
		return new ClusterRegistry();
	}

	@Bean
	public ClusterRestTemplate clusterRestTemplate() {
		return new UIModeClusterRestTemplate();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new JdbcJobManager();
	}

	@Bean
	public JobIdCache jobIdCache(RedisConnectionFactory redisConnectionFactory, RedisSerializer<?> redisSerializer) {
		return new JobIdCache(redisConnectionFactory, redisSerializer);
	}

	@Bean("job-listener-container")
	public LifecycleListenerContainer jobListenerContainer(@Value("${spring.application.cluster.name}") String clusterName,
			RedisMessageSender redisMessageSender) {
		LifecycleListenerContainer jobListenerContainer = new LifecycleListenerContainer(clusterName, redisMessageSender);
		redisMessageSender.subscribeChannel("job-listener-container", jobListenerContainer);
		return jobListenerContainer;
	}

	@Bean
	public JobAdmin jobAdmin() {
		return new ServerModeJobAdmin();
	}
}
