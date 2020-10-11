package com.github.paganini2008.springworld.jobclick.server;

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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.jobclick.BeanNames;
import com.github.paganini2008.springworld.jobclick.ConditionalOnServerMode;
import com.github.paganini2008.springworld.jobclick.CurrentThreadRetryPolicy;
import com.github.paganini2008.springworld.jobclick.DeclaredJobListenerBeanPostProcessor;
import com.github.paganini2008.springworld.jobclick.DefaultJobDependencyObservable;
import com.github.paganini2008.springworld.jobclick.DefaultScheduleManager;
import com.github.paganini2008.springworld.jobclick.DefaultSchedulerStarterListener;
import com.github.paganini2008.springworld.jobclick.ExternalJobBeanLoader;
import com.github.paganini2008.springworld.jobclick.FailoverRetryPolicy;
import com.github.paganini2008.springworld.jobclick.InternalJobBeanLoader;
import com.github.paganini2008.springworld.jobclick.JdbcJobManager;
import com.github.paganini2008.springworld.jobclick.JdbcLogManager;
import com.github.paganini2008.springworld.jobclick.JdbcStopWatch;
import com.github.paganini2008.springworld.jobclick.JobAdmin;
import com.github.paganini2008.springworld.jobclick.JobAdminController;
import com.github.paganini2008.springworld.jobclick.JobBeanLoader;
import com.github.paganini2008.springworld.jobclick.JobDependencyDetector;
import com.github.paganini2008.springworld.jobclick.JobDependencyFutureListener;
import com.github.paganini2008.springworld.jobclick.JobDependencyObservable;
import com.github.paganini2008.springworld.jobclick.JobExecutor;
import com.github.paganini2008.springworld.jobclick.JobFutureHolder;
import com.github.paganini2008.springworld.jobclick.JobIdCache;
import com.github.paganini2008.springworld.jobclick.JobManager;
import com.github.paganini2008.springworld.jobclick.LifeCycleListenerContainer;
import com.github.paganini2008.springworld.jobclick.LoadBalancedJobBeanProcessor;
import com.github.paganini2008.springworld.jobclick.LogManager;
import com.github.paganini2008.springworld.jobclick.NotManagedJobBeanInitializer;
import com.github.paganini2008.springworld.jobclick.RetryPolicy;
import com.github.paganini2008.springworld.jobclick.ScheduleAdmin;
import com.github.paganini2008.springworld.jobclick.ScheduleManager;
import com.github.paganini2008.springworld.jobclick.Scheduler;
import com.github.paganini2008.springworld.jobclick.SchedulerDeadlineProcessor;
import com.github.paganini2008.springworld.jobclick.SchedulerErrorHandler;
import com.github.paganini2008.springworld.jobclick.SchedulerStarterListener;
import com.github.paganini2008.springworld.jobclick.SpringScheduler;
import com.github.paganini2008.springworld.jobclick.StopWatch;
import com.github.paganini2008.springworld.jobclick.TimestampTraceIdGenerator;
import com.github.paganini2008.springworld.jobclick.TraceIdGenerator;
import com.github.paganini2008.springworld.jobclick.OnServerModeCondition.ServerMode;
import com.github.paganini2008.springworld.jobclick.cron4j.Cron4jScheduler;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

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
		log.info("<<<                                                  >>>");
		log.info("<<<                Crontab v2.0-RC4                  >>>");
		log.info("<<<              Current Job Deploy Mode             >>>");
		log.info("<<<                 [Server Mode]                    >>>");
		log.info("<<<                                                  >>>");
	}

	@Configuration
	@ConditionalOnServerMode(ServerMode.PRODUCER)
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
	@ConditionalOnServerMode(ServerMode.PRODUCER)
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

	@Configuration
	@Import({ ClusterRegistryController.class, JobManagerController.class, JobAdminController.class })
	@ConditionalOnServerMode(ServerMode.PRODUCER)
	public static class ProducerModeConfig {

		@Bean
		public ClusterRegistry clusterRegistry() {
			return new ClusterRegistry();
		}

		@ConditionalOnMissingBean(ClusterRestTemplate.class)
		@Bean
		public ClusterRestTemplate clusterRestTemplate() {
			return new ProducerModeRestTemplate();
		}

		@Bean
		public SchedulerStarterListener schedulerStarterListener() {
			return new DefaultSchedulerStarterListener();
		}

		@Bean
		public DeclaredJobListenerBeanPostProcessor declaredJobListenerBeanPostProcessor() {
			return new DeclaredJobListenerBeanPostProcessor();
		}

		@Bean
		public NotManagedJobBeanInitializer producerModeJobBeanInitializer() {
			return new ProducerModeJobBeanInitializer();
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
		public JobAdmin jobAdmin() {
			return new ServerModeJobAdmin();
		}

		@Bean
		public ScheduleAdmin scheduleAdmin() {
			return new ServerModeScheduleAdmin();
		}

		@Bean
		public JobDependencyObservable jobDependencyObservable() {
			return new DefaultJobDependencyObservable();
		}

		@Bean
		public JobDependencyDetector jobDependencyDetector() {
			return new JobDependencyDetector();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(ScheduleManager.class)
		public ScheduleManager scheduleManager() {
			return new DefaultScheduleManager();
		}

		@Bean
		public JobFutureHolder jobFutureHolder() {
			return new JobFutureHolder();
		}

		@Bean
		public JobBeanLoader jobBeanLoader() {
			return new ServerModeJobBeanLoader();
		}

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			return new ProducerModeJobExecutor();
		}

		@Bean("scheduler-error-handler")
		public ErrorHandler schedulerErrorHandler() {
			return new SchedulerErrorHandler();
		}

		@Bean
		public SchedulerDeadlineProcessor schedulerDeadlineProcessor() {
			return new SchedulerDeadlineProcessor();
		}

		@Bean("job-listener-container")
		public LifeCycleListenerContainer jobListenerContainer(@Value("${spring.application.cluster.name}") String clusterName,
				RedisMessageSender redisMessageSender) {
			LifeCycleListenerContainer jobListenerContainer = new LifeCycleListenerContainer(clusterName, redisMessageSender);
			redisMessageSender.subscribeChannel("job-listener-container", jobListenerContainer);
			return jobListenerContainer;
		}

		@Bean
		public JobDependencyFutureListener jobDependencyFutureListener() {
			return new JobDependencyFutureListener();
		}

		@Bean
		public JobIdCache jobIdCache(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
			return new JobIdCache(redisConnectionFactory, redisSerializer);
		}

		@Bean
		public TraceIdGenerator traceIdGenerator(RedisConnectionFactory redisConnectionFactory) {
			return new TimestampTraceIdGenerator(redisConnectionFactory);
		}

		@Bean
		public LogManager logManager() {
			return new JdbcLogManager();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	@Configuration
	@Import({ ConsumerModeController.class })
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	public static class ConsumerModeConfig {

		@Bean
		public ConsumerModeStarterListener consumerModeStarterListener() {
			return new ConsumerModeStarterListener();
		}

		@ConditionalOnMissingBean(ClusterRestTemplate.class)
		@Bean
		public ClusterRestTemplate clusterRestTemplate() {
			return new ConsumerModeRestTemplate();
		}

		@Bean
		public ConsumerModeJobBeanInitializer consumerModeJobBeanListener() {
			return new ConsumerModeJobBeanInitializer();
		}

		@Bean
		public SchedulerStarterListener consumerModeSchedulerStarterListener() {
			return new ConsumerModeSchedulerStarterListener();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		public JobManager jobManager() {
			return new RestJobManager();
		}

		@Bean
		public StopWatch stopWatch() {
			return new RestStopWatch();
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
		public JobFutureHolder jobFutureHolder() {
			return new JobFutureHolder();
		}

		@Bean
		public JobAdmin jobAdmin() {
			return new ConsumerModeJobAdmin();
		}

		@Bean
		public TraceIdGenerator traceIdGenerator() {
			return new RestTraceIdGenerator();
		}

		@Bean
		public LogManager logManager() {
			return new RestLogManager();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "master-slave")
	public static class MasterSlaveConfig {

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			ConsumerModeJobExecutor jobExecutor = new ConsumerModeJobExecutor();
			jobExecutor.setThreadPool(Executors.newCachedThreadPool(new PooledThreadFactory("job-executor-threads")));
			return jobExecutor;
		}

		@Bean(BeanNames.INTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}

		@Bean
		public RetryPolicy retryPolicy() {
			return new CurrentThreadRetryPolicy();
		}
	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "spring.application.cluster.scheduler.runningMode", havingValue = "loadbalance", matchIfMissing = true)
	public static class LoadBalanceConfig {

		@Bean(BeanNames.INTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}

		@Bean(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader externalJobBeanLoader() {
			return new ExternalJobBeanLoader();
		}

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			return new ConsumerModeLoadBalancer();
		}

		@Bean(BeanNames.TARGET_JOB_EXECUTOR)
		public JobExecutor consumerModeJobExecutor() {
			ConsumerModeJobExecutor jobExecutor = new ConsumerModeJobExecutor();
			jobExecutor.setThreadPool(Executors.newCachedThreadPool(new PooledThreadFactory("job-executor-threads")));
			return jobExecutor;
		}

		@Bean
		public LoadBalancedJobBeanProcessor loadBalancedJobBeanProcessor() {
			return new LoadBalancedJobBeanProcessor();
		}

		@Bean
		public RetryPolicy retryPolicy() {
			return new FailoverRetryPolicy();
		}

	}

}