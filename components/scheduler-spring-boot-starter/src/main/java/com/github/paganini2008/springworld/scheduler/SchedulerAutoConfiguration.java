package com.github.paganini2008.springworld.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.cluster.ApplicationClusterConfig;

/**
 * 
 * SchedulerAutoConfiguration
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(ApplicationClusterConfig.class)
@Import({ JobBeanPostProcessor.class, JobLauncherListener.class, HealthCheckJob.class, JobManagerController.class })
public class SchedulerAutoConfiguration {

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(JobStore.class)
	public JobStore jobStore() {
		return new EmbeddedJobStore();
	}

	@Bean
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new JdbcJobManager();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "spring", matchIfMissing = true)
	public Scheduler springScheduler() {
		return new SpringScheduler();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "cron4j", matchIfMissing = true)
	public Scheduler cron4jScheduler() {
		return new Cron4jScheduler();
	}

}
