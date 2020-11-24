package com.github.paganini2008.springdessert.jobsoup.server;

import java.util.Properties;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.devtools.multithreads.ThreadPoolBuilder;
import com.github.paganini2008.springdessert.jobsoup.BeanNames;
import com.github.paganini2008.springdessert.jobsoup.ConditionalOnServerMode;
import com.github.paganini2008.springdessert.jobsoup.CurrentThreadRetryPolicy;
import com.github.paganini2008.springdessert.jobsoup.DeclaredJobListenerBeanPostProcessor;
import com.github.paganini2008.springdessert.jobsoup.DefaultSchedulerStarterListener;
import com.github.paganini2008.springdessert.jobsoup.ExternalJobBeanLoader;
import com.github.paganini2008.springdessert.jobsoup.FailoverRetryPolicy;
import com.github.paganini2008.springdessert.jobsoup.InternalJobBeanLoader;
import com.github.paganini2008.springdessert.jobsoup.JdbcJobManager;
import com.github.paganini2008.springdessert.jobsoup.JdbcLogManager;
import com.github.paganini2008.springdessert.jobsoup.JdbcStopWatch;
import com.github.paganini2008.springdessert.jobsoup.JobAdmin;
import com.github.paganini2008.springdessert.jobsoup.JobAdminController;
import com.github.paganini2008.springdessert.jobsoup.JobBeanInitializer;
import com.github.paganini2008.springdessert.jobsoup.JobBeanLoader;
import com.github.paganini2008.springdessert.jobsoup.JobDeadlineNotification;
import com.github.paganini2008.springdessert.jobsoup.JobDependencyFutureListener;
import com.github.paganini2008.springdessert.jobsoup.JobExecutor;
import com.github.paganini2008.springdessert.jobsoup.JobFutureHolder;
import com.github.paganini2008.springdessert.jobsoup.JobIdCache;
import com.github.paganini2008.springdessert.jobsoup.JobManager;
import com.github.paganini2008.springdessert.jobsoup.JobManagerConnectionFactory;
import com.github.paganini2008.springdessert.jobsoup.JobRuntimeListenerContainer;
import com.github.paganini2008.springdessert.jobsoup.JobTimeoutResolver;
import com.github.paganini2008.springdessert.jobsoup.LifeCycleListenerContainer;
import com.github.paganini2008.springdessert.jobsoup.LoadBalancedJobBeanProcessor;
import com.github.paganini2008.springdessert.jobsoup.LogManager;
import com.github.paganini2008.springdessert.jobsoup.MailContentSource;
import com.github.paganini2008.springdessert.jobsoup.PrintableMailContentSource;
import com.github.paganini2008.springdessert.jobsoup.RetryPolicy;
import com.github.paganini2008.springdessert.jobsoup.ScheduleAdmin;
import com.github.paganini2008.springdessert.jobsoup.ScheduleManager;
import com.github.paganini2008.springdessert.jobsoup.Scheduler;
import com.github.paganini2008.springdessert.jobsoup.SchedulerErrorHandler;
import com.github.paganini2008.springdessert.jobsoup.SchedulerStarterListener;
import com.github.paganini2008.springdessert.jobsoup.SerialDependencyListener;
import com.github.paganini2008.springdessert.jobsoup.SerialDependencyScheduler;
import com.github.paganini2008.springdessert.jobsoup.SerialDependencySchedulerImpl;
import com.github.paganini2008.springdessert.jobsoup.SpringScheduler;
import com.github.paganini2008.springdessert.jobsoup.StopWatch;
import com.github.paganini2008.springdessert.jobsoup.TimestampTraceIdGenerator;
import com.github.paganini2008.springdessert.jobsoup.TraceIdGenerator;
import com.github.paganini2008.springdessert.jobsoup.cron4j.Cron4jScheduler;
import com.github.paganini2008.springdessert.jobsoup.utils.JavaMailService;

import lombok.Setter;

/**
 * 
 * ServerModeConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
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

		@Bean
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

		@Bean
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

	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@Import({ ConsumerModeController.class })
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

		@Bean
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

	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
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

	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
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

	@Setter
	@Configuration
	@ConditionalOnServerMode(ServerMode.CONSUMER)
	@ConfigurationProperties(prefix = "jobsoup.mail")
	public static class JavaMailConfig {

		private String host;
		private String username;
		private String password;
		private String defaultEncoding;

		@Bean
		public JavaMailSender jobMailSender() {
			JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
			javaMailSender.setHost(host);
			javaMailSender.setUsername(username);
			javaMailSender.setPassword(password);
			javaMailSender.setDefaultEncoding(defaultEncoding);
			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.auth", true);
			javaMailProperties.put("mail.smtp.starttls.enable", false);
			javaMailProperties.put("mail.smtp.starttls.required", false);
			javaMailProperties.put("mail.smtp.timeout", 60000);
			javaMailSender.setJavaMailProperties(javaMailProperties);
			return javaMailSender;
		}
		
		@Bean
		@ConditionalOnBean(JavaMailSender.class)
		public JavaMailService javaMailService() {
			return new JavaMailService();
		}

		@Bean
		@ConditionalOnMissingBean(MailContentSource.class)
		public MailContentSource printableMailContentSource() {
			return new PrintableMailContentSource();
		}
	}
	
	@Bean
	public JobRuntimeListenerContainer jobRuntimeListenerContainer() {
		return new JobRuntimeListenerContainer();
	}

}
