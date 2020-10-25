package com.github.paganini2008.springworld.jobstorm;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.ThreadPoolTaskExecutor;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.PooledConnectionFactory;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.devtools.multithreads.ThreadPoolBuilder;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.jobstorm.cron4j.Cron4jScheduler;
import com.github.paganini2008.springworld.jobstorm.server.ConsumerModeJobExecutor;
import com.github.paganini2008.springworld.jobstorm.utils.JavaMailService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "jobstorm.deploy.mode", havingValue = "embedded", matchIfMissing = true)
@Import({ JobAdminController.class })
public class EmbeddedModeConfiguration {

	public EmbeddedModeConfiguration() {
		log.info("<<<                                                  >>>");
		log.info("<<<                Crontab v2.0-RC4                  >>>");
		log.info("<<<              Current Job Deploy Mode             >>>");
		log.info("<<<                 [Embedded Mode]                  >>>");
		log.info("<<<                                                  >>>");
	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	@Configuration
	@ConditionalOnBean(ClusterMulticastGroup.class)
	@ConditionalOnProperty(name = "jobstorm.scheduler.running.mode", havingValue = "loadbalance", matchIfMissing = true)
	public static class LoadBalanceConfig {

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor() {
			return new EmbeddedModeLoadBalancer();
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
	@ConditionalOnProperty(name = "jobstorm.scheduler.running.mode", havingValue = "master-slave")
	public static class MasterSlaveConfig {

		@Bean(BeanNames.INTERNAL_JOB_BEAN_LOADER)
		public JobBeanLoader jobBeanLoader() {
			return new InternalJobBeanLoader();
		}

		@Bean(BeanNames.MAIN_JOB_EXECUTOR)
		public JobExecutor jobExecutor(@Qualifier("executorThreadPool") Executor threadPool) {
			EmbeddedModeJobExecutor jobExecutor = new EmbeddedModeJobExecutor();
			jobExecutor.setThreadPool(threadPool);
			return jobExecutor;
		}

		@Bean
		public RetryPolicy retryPolicy() {
			return new CurrentThreadRetryPolicy();
		}
	}

	@Bean
	public Executor executorThreadPool(@Value("${jobstorm.scheduler.executor.poolSize:16}") int maxPoolSize) {
		return ThreadPoolBuilder.common(maxPoolSize).setTimeout(-1L).setQueueSize(Integer.MAX_VALUE)
				.setThreadFactory(new PooledThreadFactory("job-executor-threads")).build();
	}

	@Configuration
	@ConditionalOnProperty(name = "jobstorm.scheduler.engine", havingValue = "spring")
	public static class SpringSchedulerConfig {

		@Value("${jobstorm.scheduler.poolSize:16}")
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
	@ConditionalOnProperty(name = "jobstorm.scheduler.engine", havingValue = "cron4j", matchIfMissing = true)
	public static class Cron4jSchedulerConfig {

		@Value("${jobstorm.scheduler.poolSize:16}")
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
	public JobBeanInitializer embeddedModeJobBeanInitializer() {
		return new EmbeddedModeJobBeanInitializer();
	}

	@Bean("scheduler-starter-listener-container")
	public SchedulerStarterListener schedulerStarterListener() {
		return new DefaultSchedulerStarterListener();
	}

	@Bean
	@ConditionalOnMissingBean(ConnectionFactory.class)
	public ConnectionFactory connectionFactory(DataSource dataSource) {
		return new PooledConnectionFactory(dataSource);
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
	public SerialDependencyScheduler serialDependencyScheduler() {
		return new SerialDependencySchedulerImpl();
	}

	@Bean
	public SerialDependencyListener serialDependencyListener() {
		return new SerialDependencyListener();
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

	@Bean
	public LifeCycleListenerContainer lifeCycleListenerContainer() {
		return new EmbeddedModeLifeCycleListenerContainer();
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
	public JobRuntimeListenerContainer jobRuntimeListenerContainer() {
		return new JobRuntimeListenerContainer();
	}

	@Bean
	@ConditionalOnClass({ JavaMailSenderImpl.class, FreeMarkerConfigurer.class })
	public JavaMailService javaMailService() {
		return new JavaMailService();
	}

}
