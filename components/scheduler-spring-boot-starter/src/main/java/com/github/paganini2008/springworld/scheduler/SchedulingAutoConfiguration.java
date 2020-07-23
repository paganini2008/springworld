package com.github.paganini2008.springworld.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.cluster.ApplicationClusterConfig;

/**
 * 
 * SchedulingAutoConfiguration
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(ApplicationClusterConfig.class)
@Import({ JobBeanPostProcessor.class, JobLauncherListener.class, HealthCheckJob.class, JobManagerController.class })
public class SchedulingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new JdbcJobManager();
	}

}
