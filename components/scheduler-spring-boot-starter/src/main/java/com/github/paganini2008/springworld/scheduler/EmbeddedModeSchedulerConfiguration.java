package com.github.paganini2008.springworld.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeSchedulerConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.mode", havingValue = "embedded", matchIfMissing = true)
@Import({ JobManagerController.class, ScheduleManagerController.class })
public class EmbeddedModeSchedulerConfiguration {

	@Configuration
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.loadbalance", havingValue = "true")
	public static class LoadBalancingConfig {

		@Primary
		@Bean
		public JobExecutor loadBalancedJobExecutor() {
			return new LoadBalancedJobExecutor();
		}

		@Bean("target-job-executor")
		public JobExecutor directJobExecutor() {
			return new ConsumerModeJobExecutor();
		}

		@Bean
		public LoadBalancedJobBeanProcessor loadBalancedJobBeanProcessor() {
			return new LoadBalancedJobBeanProcessor();
		}

	}

	@Slf4j
	@Configuration
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "spring", matchIfMissing = true)
	public static class SpringSchedulerConfig {

		@Value("${spring.application.cluster.scheduler.poolSize:8}")
		private int poolSize;

		@Bean
		public Scheduler springScheduler() {
			return new SpringScheduler();
		}

		@Bean("cluster-job-scheduler")
		public TaskScheduler taskScheduler() {
			ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
			threadPoolTaskScheduler.setPoolSize(poolSize);
			threadPoolTaskScheduler.setThreadNamePrefix("cluster-task-scheduler-");
			threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
			threadPoolTaskScheduler.setErrorHandler(e -> {
				log.error(e.getMessage(), e);
			});
			return threadPoolTaskScheduler;
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "cron4j")
	public static class Cron4jSchedulerConfig {

		@Value("${spring.application.cluster.scheduler.poolSize:8}")
		private int poolSize;

		@Bean
		public Scheduler cron4jScheduler() {
			return new Cron4jScheduler();
		}

		@Bean("cluster-job-scheduler")
		public TaskExecutor taskExecutor() {
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize,
					new PooledThreadFactory("cluster-task-scheduler-"));
			return new ThreadPoolTaskExecutor(executor);
		}
	}

	@Bean
	public DeclaredJobBeanPostProcessor declaredJobBeanPostProcessor() {
		return new DeclaredJobBeanPostProcessor();
	}

	@Bean
	public NotManagedJobBeanInitializer notManagedJobBeanInitializer() {
		return new NotManagedJobBeanInitializer();
	}

	@Bean
	public JobSchedulerListener jobSchedulerListener() {
		return new JobSchedulerListener();
	}

	@Bean
	@ConditionalOnMissingBean(JobBeanLoader.class)
	public JobBeanLoader jobBeanLoader() {
		return new EmbeddedModeJobBeanLoader();
	}
	
	@Bean
	@ConditionalOnMissingBean(JobExecutor.class)
	public JobExecutor jobExecutor() {
		return new EmbeddedModeJobExecutor();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new JdbcJobManager();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(ScheduleManager.class)
	public ScheduleManager scheduleManager() {
		return new DefaultScheduleManager();
	}

	@Bean
	public JobDependency jobDependency() {
		return new JobDependency();
	}

}
