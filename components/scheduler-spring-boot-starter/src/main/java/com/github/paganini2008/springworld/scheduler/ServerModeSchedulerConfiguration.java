package com.github.paganini2008.springworld.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.util.ErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeSchedulerConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.mode", havingValue = "server")
public class ServerModeSchedulerConfiguration {

	public ServerModeSchedulerConfiguration() {
		log.info("Cluster scheduler mode is server.");
	}

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
		public TaskScheduler taskScheduler(@Qualifier("scheduler-error-handler") ErrorHandler errorHandler) {
			ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
			threadPoolTaskScheduler.setPoolSize(poolSize);
			threadPoolTaskScheduler.setThreadNamePrefix("cluster-task-scheduler-");
			threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
			threadPoolTaskScheduler.setErrorHandler(errorHandler);
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

	@Bean("scheduler-error-handler")
	public ErrorHandler schedulerErrorHandler() {
		return new SchedulerErrorHandler();
	}

	@Configuration
	@Import({ JobManagerController.class })
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.side", havingValue = "producer")
	public static class ProducerModeConfig {

		@Bean
		public JobSchedulerListener jobSchedulerListener() {
			return new JobSchedulerListener();
		}

		@Bean
		@ConditionalOnMissingBean(JobBeanInitializer.class)
		public JobBeanInitializer jobBeanInitializer() {
			return new NotManagedJobBeanInitializer();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(JobManager.class)
		public JobManager jobManager() {
			return new JdbcJobManager();
		}

		@Bean
		public JobDependency jobDependency() {
			return new JobDependency();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(ScheduleManager.class)
		public ScheduleManager scheduleManager() {
			return new DefaultScheduleManager();
		}

		@Bean
		public JobBeanLoader jobBeanLoader() {
			return new ServerModeJobBeanLoader();
		}

		@Bean
		public JobExecutor jobExecutor() {
			return new ProducerModeJobExecutor();
		}

		@Bean("scheduler-httpclient")
		@ConditionalOnMissingBean(RestTemplate.class)
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}

	}

	@Configuration
	@Import({ JobController.class })
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.side", havingValue = "consumer", matchIfMissing = true)
	public static class ConsumerModeConfig {

		@Bean
		public NotScheduledJobBeanPostProcessor notScheduledJobBeanPostProcessor() {
			return new NotScheduledJobBeanPostProcessor();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(JobManager.class)
		public JobManager jobManager() {
			return new JdbcJobManager();
		}

		@Bean
		public JobDependency jobDependency() {
			return new JobDependency();
		}

		@Bean
		@ConditionalOnMissingBean(JobExecutor.class)
		public JobExecutor jobExecutor() {
			return new ConsumerModeJobExecutor();
		}

		@Bean
		public JobBeanLoader jobBeanLoader() {
			return new EmbeddedModeJobBeanLoader();
		}

	}

	@Configuration
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.loadbalance", havingValue = "true")
	public static class LoadBalancingConfig {

		@Primary
		@Bean
		public JobExecutor jobExecutor() {
			return new LoadBalancedJobExecutor();
		}

		@Bean("target-job-executor")
		public JobExecutor consumerModeJobExecutor() {
			return new ConsumerModeJobExecutor();
		}

		@Bean
		public LoadBalancedJobBeanProcessor loadBalancedJobBeanProcessor() {
			return new LoadBalancedJobBeanProcessor();
		}

	}

}
