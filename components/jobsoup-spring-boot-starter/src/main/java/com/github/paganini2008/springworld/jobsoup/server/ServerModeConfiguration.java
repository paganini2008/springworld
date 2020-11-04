package com.github.paganini2008.springworld.jobsoup.server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.devtools.multithreads.ThreadPoolBuilder;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.jobsoup.BeanNames;
import com.github.paganini2008.springworld.jobsoup.ConditionalOnServerMode;
import com.github.paganini2008.springworld.jobsoup.CurrentThreadRetryPolicy;
import com.github.paganini2008.springworld.jobsoup.DeclaredJobListenerBeanPostProcessor;
import com.github.paganini2008.springworld.jobsoup.DefaultSchedulerStarterListener;
import com.github.paganini2008.springworld.jobsoup.ExternalJobBeanLoader;
import com.github.paganini2008.springworld.jobsoup.FailoverRetryPolicy;
import com.github.paganini2008.springworld.jobsoup.InternalJobBeanLoader;
import com.github.paganini2008.springworld.jobsoup.JdbcJobManager;
import com.github.paganini2008.springworld.jobsoup.JdbcLogManager;
import com.github.paganini2008.springworld.jobsoup.JdbcStopWatch;
import com.github.paganini2008.springworld.jobsoup.JobAdmin;
import com.github.paganini2008.springworld.jobsoup.JobAdminController;
import com.github.paganini2008.springworld.jobsoup.JobBeanInitializer;
import com.github.paganini2008.springworld.jobsoup.JobBeanLoader;
import com.github.paganini2008.springworld.jobsoup.JobDeadlineNotification;
import com.github.paganini2008.springworld.jobsoup.JobDependencyFutureListener;
import com.github.paganini2008.springworld.jobsoup.JobExecutor;
import com.github.paganini2008.springworld.jobsoup.JobFutureHolder;
import com.github.paganini2008.springworld.jobsoup.JobIdCache;
import com.github.paganini2008.springworld.jobsoup.JobManager;
import com.github.paganini2008.springworld.jobsoup.JobManagerConnectionFactory;
import com.github.paganini2008.springworld.jobsoup.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobsoup.JobTimeoutResolver;
import com.github.paganini2008.springworld.jobsoup.LifeCycleListenerContainer;
import com.github.paganini2008.springworld.jobsoup.LoadBalancedJobBeanProcessor;
import com.github.paganini2008.springworld.jobsoup.LogManager;
import com.github.paganini2008.springworld.jobsoup.MailContentSource;
import com.github.paganini2008.springworld.jobsoup.OnServerModeCondition.ServerMode;
import com.github.paganini2008.springworld.jobsoup.PrintableMailContentSource;
import com.github.paganini2008.springworld.jobsoup.RetryPolicy;
import com.github.paganini2008.springworld.jobsoup.ScheduleAdmin;
import com.github.paganini2008.springworld.jobsoup.ScheduleManager;
import com.github.paganini2008.springworld.jobsoup.Scheduler;
import com.github.paganini2008.springworld.jobsoup.SchedulerErrorHandler;
import com.github.paganini2008.springworld.jobsoup.SchedulerStarterListener;
import com.github.paganini2008.springworld.jobsoup.SerialDependencyListener;
import com.github.paganini2008.springworld.jobsoup.SerialDependencyScheduler;
import com.github.paganini2008.springworld.jobsoup.SerialDependencySchedulerImpl;
import com.github.paganini2008.springworld.jobsoup.SpringScheduler;
import com.github.paganini2008.springworld.jobsoup.StopWatch;
import com.github.paganini2008.springworld.jobsoup.TimestampTraceIdGenerator;
import com.github.paganini2008.springworld.jobsoup.TraceIdGenerator;
import com.github.paganini2008.springworld.jobsoup.cron4j.Cron4jScheduler;
import com.github.paganini2008.springworld.jobsoup.utils.JavaMailService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@ConditionalOnWebApplication
@Configuration
public class ServerModeConfiguration {

	@Configuration
	@ConditionalOnServerMode(ServerMode.PRODUCER)
	@ConditionalOnProperty(name = "jobsoup.scheduler.engine", havingValue = "spring")
	public static class SpringSchedulerConfig {

		@Value("${jobsoup.scheduler.poolSize:16}")
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
	@ConditionalOnProperty(name = "jobsoup.scheduler.engine", havingValue = "cron4j", matchIfMissing = true)
	public static class Cron4jSchedulerConfig {

		@Value("${jobsoup.scheduler.poolSize:16}")
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
		public JobBeanInitializer producerModeJobBeanInitializer() {
			return new ProducerModeJobBeanInitializer();
		}

		@Bean
		public ConnectionFactory connectionFactory(DataSource dataSource) {
			return new JobManagerConnectionFactory(dataSource);
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
		public SerialDependencyScheduler serialDependencyScheduler() {
			return new SerialDependencySchedulerImpl();
		}

		@Bean
		public SerialDependencyListener jobDependencyDetector() {
			return new SerialDependencyListener();
		}

		@Bean(initMethod = "configure", destroyMethod = "close")
		@ConditionalOnMissingBean(ScheduleManager.class)
		public ScheduleManager scheduleManager() {
			return new ServerModeScheduleManager();
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
		public JobDeadlineNotification jobDeadlineNotification() {
			return new JobDeadlineNotification();
		}

		@Bean
		public LifeCycleListenerContainer lifeCycleListenerContainer() {
			return new ServerModeLifeCycleListenerContainer();
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

		@Bean
		public JobTimeoutResolver timeoutResolver() {
			return new JobTimeoutResolver();
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
		public SerialDependencyScheduler serialDependencyScheduler() {
			return new SerialDependencySchedulerImpl();
		}

		@Bean
		public SerialDependencyListener serialDependencyListener() {
			return new SerialDependencyListener();
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

		@Bean
		@ConditionalOnBean(JavaMailSenderImpl.class)
		public JavaMailService javaMailService() {
			return new JavaMailService();
		}

		@Bean
		@ConditionalOnMissingBean(MailContentSource.class)
		public MailContentSource printableMailContentSource() {
			return new PrintableMailContentSource();
		}

		@Bean
		public Executor executorThreadPool(@Value("${jobsoup.scheduler.executor.poolSize:16}") int maxPoolSize) {
			return ThreadPoolBuilder.common(maxPoolSize).setTimeout(-1L).setQueueSize(Integer.MAX_VALUE)
					.setThreadFactory(new PooledThreadFactory("job-executor-threads")).build();
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "jobsoup.scheduler.running.mode", havingValue = "master-slave")
	public static class MasterSlaveConfig {

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor(@Qualifier("executorThreadPool") Executor threadPool) {
			ConsumerModeJobExecutor jobExecutor = new ConsumerModeJobExecutor();
			jobExecutor.setThreadPool(threadPool);
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
	@ConditionalOnProperty(name = "jobsoup.scheduler.running.mode", havingValue = "loadbalance", matchIfMissing = true)
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
		public JobExecutor consumerModeJobExecutor(@Qualifier("executorThreadPool") Executor threadPool) {
			ConsumerModeJobExecutor jobExecutor = new ConsumerModeJobExecutor();
			jobExecutor.setThreadPool(threadPool);
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

	@Bean
	public JobRuntimeListenerContainer jobRuntimeListenerContainer() {
		return new JobRuntimeListenerContainer();
	}

}
