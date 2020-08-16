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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.scheduler.OnServerModeCondition.ServerMode;

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
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.deployMode", havingValue = "server")
public class ServerModeSchedulerConfiguration {

	public ServerModeSchedulerConfiguration() {
		log.info("Cluster scheduler mode is server.");
	}

	@Configuration
	@ConditionalOnServerMode(ServerMode.PRODUCER)
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
	@ConditionalOnServerMode(ServerMode.PRODUCER)
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

	@Configuration
	@Import({ JobAdminController.class })
	@ConditionalOnServerMode(ServerMode.PRODUCER)
	public static class ProducerModeConfig {

		@Bean
		public JobSchedulerStarterListener jobSchedulerStarterListener() {
			return new JobSchedulerStarterListener();
		}

		@Bean
		@ConditionalOnMissingBean(NotManagedJobBeanInitializer.class)
		public NotManagedJobBeanInitializer serverModeJobBeanInitializer() {
			return new ServerModeJobBeanInitializer();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(JobManager.class)
		public JobManager jobManager() {
			return new JdbcJobManager();
		}

		@Bean
		public JobAdmin serverModeJobAdmin() {
			return new ServerModeJobAdmin();
		}

		@Bean
		public JobDependencyObservable jobDependencyObservable() {
			return new JobDependencyObservable();
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

		@Bean("main-job-executor")
		public JobExecutor jobExecutor() {
			return new ProducerModeJobExecutor();
		}

		@Bean("scheduler-httpclient")
		@ConditionalOnMissingBean(RestTemplate.class)
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@Bean("scheduler-error-handler")
		public ErrorHandler schedulerErrorHandler() {
			return new SchedulerErrorHandler();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	@Configuration
	@Import({ JobController.class, JobAdminController.class })
	@ConditionalOnServerMode(ServerMode.CONSUMER)
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
		public JobDependencyObservable jobDependency() {
			return new JobDependencyObservable();
		}

		@Bean
		public JobDependencyDetector jobDependencyDetector() {
			return new JobDependencyDetector();
		}

		@Bean
		public JobAdmin jobAdmin() {
			return new EmbeddedModeJobAdmin();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "master-slave")
	public static class MasterSlaveConfig {

		@Bean("main-job-executor")
		public JobExecutor jobExecutor() {
			return new ConsumerModeJobExecutor();
		}

		@Bean("internal-job-bean-loader")
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}
	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "loadbalance", matchIfMissing = true)
	public static class LoadBalanceConfig {

		@Bean("internal-job-bean-loader")
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}

		@Bean("external-job-bean-loader")
		public JobBeanLoader externalJobBeanLoader() {
			return new ExternalJobBeanLoader();
		}

		@Bean("main-job-executor")
		public JobExecutor jobExecutor() {
			return new ConsumerModeLoadBalancer();
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
