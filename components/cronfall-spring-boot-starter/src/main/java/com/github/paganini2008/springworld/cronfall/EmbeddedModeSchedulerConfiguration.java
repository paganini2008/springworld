package com.github.paganini2008.springworld.cronfall;

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

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.cronfall.server.ConsumerModeJobExecutor;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeSchedulerConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.deployMode", havingValue = "embedded", matchIfMissing = true)
@Import({ JobAdminController.class })
public class EmbeddedModeSchedulerConfiguration {

	public EmbeddedModeSchedulerConfiguration() {
		log.info("<<<                                                  >>>");
		log.info("<<<                Crontab v2.0-RC4                  >>>");
		log.info("<<<              Current Job Deploy Mode             >>>");
		log.info("<<<                 [Embedded Mode]                  >>>");
		log.info("<<<                                                  >>>");
	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "loadbalance", matchIfMissing = true)
	public static class LoadBalanceConfig {

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			return new EmbeddedModeLoadBalancer();
		}

		@Bean(BeanNames.TARGET_JOB_EXECUTOR)
		public JobExecutor consumerModeJobExecutor() {
			return new ConsumerModeJobExecutor();
		}

		@Bean
		public LoadBalancedJobBeanProcessor loadBalancedJobBeanProcessor() {
			return new LoadBalancedJobBeanProcessor();
		}

		@Bean(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader externalJobBeanLoader() {
			return new ExternalJobBeanLoader();
		}

		@Bean(BeanNames.INTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}
		
		@Bean
		public RetryPolicy retryPolicy() {
			return new FailoverRetryPolicy();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "master-slave")
	public static class MasterSlaveConfig {

		@Bean(BeanNames.INTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			return new EmbeddedModeJobExecutor();
		}

		@Bean
		public RetryPolicy retryPolicy() {
			return new CurrentThreadRetryPolicy();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "spring")
	public static class SpringSchedulerConfig {

		@Value("${spring.application.cluster.scheduler.poolSize:16}")
		private int poolSize;

		@Bean
		public Scheduler springScheduler() {
			return new SpringScheduler();
		}

		@Bean(name = BeanNames.CLUSTER_JOB_SCHEDULER, destroyMethod = "shutdown")
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
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.engine", havingValue = "cron4j", matchIfMissing = true)
	public static class Cron4jSchedulerConfig {

		@Value("${spring.application.cluster.scheduler.poolSize:16}")
		private int poolSize;

		@Bean
		public Scheduler cron4jScheduler() {
			return new Cron4jScheduler();
		}

		@ConditionalOnMissingBean(TaskExecutor.class)
		@Bean(name = BeanNames.CLUSTER_JOB_SCHEDULER, destroyMethod = "close")
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

	@Bean
	public DeclaredJobBeanPostProcessor declaredJobBeanPostProcessor() {
		return new DeclaredJobBeanPostProcessor();
	}

	@Bean
	public DeclaredJobListenerBeanPostProcessor declaredJobListenerBeanPostProcessor() {
		return new DeclaredJobListenerBeanPostProcessor();
	}

	@Bean
	public NotManagedJobBeanInitializer embeddedModeJobBeanInitializer() {
		return new EmbeddedModeJobBeanInitializer();
	}

	@Bean("scheduler-starter-listener-container")
	public SchedulerStarterListener schedulerStarterListener() {
		return new DefaultSchedulerStarterListener();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new JdbcJobManager();
	}

	@Bean
	@ConditionalOnMissingBean(StopWatch.class)
	public StopWatch stopWatch() {
		return new JdbcStopWatch();
	}

	@Bean
	public JobFutureHolder jobFutureHolder() {
		return new JobFutureHolder();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(ScheduleManager.class)
	public ScheduleManager scheduleManager() {
		return new DefaultScheduleManager();
	}

	@Bean
	public JobDependencyObservable jobDependencyObservable() {
		return new DefaultJobDependencyObservable();
	}

	@Bean
	public JobDependencyDetector jobDependencyDetector() {
		return new JobDependencyDetector();
	}

	@Bean
	public JobDependencyFutureListener jobDependencyFutureListener() {
		return new JobDependencyFutureListener();
	}

	@Bean
	public JobAdmin embeddedModeJobAdmin() {
		return new EmbeddedModeJobAdmin();
	}

	@Bean
	public ScheduleAdmin scheduleAdmin() {
		return new EmbeddedModeScheduleAdmin();
	}

	@Bean
	public SchedulerDeadlineProcessor schedulerDeadlineProcessor() {
		return new SchedulerDeadlineProcessor();
	}

	@Bean("job-listener-container")
	public LifecycleListenerContainer jobListenerContainer(@Value("${spring.application.cluster.name}") String clusterName,
			RedisMessageSender redisMessageSender) {
		LifecycleListenerContainer jobListenerContainer = new LifecycleListenerContainer(clusterName, redisMessageSender);
		redisMessageSender.subscribeChannel("job-listener-container", jobListenerContainer);
		return jobListenerContainer;
	}

}
