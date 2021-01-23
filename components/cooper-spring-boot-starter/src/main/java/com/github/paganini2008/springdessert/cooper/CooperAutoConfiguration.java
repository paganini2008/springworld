package com.github.paganini2008.springdessert.cooper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.xtransport.HttpTransportClient;
import com.github.paganini2008.xtransport.TransportClient;

/**
 * 
 * CooperAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
public class CooperAutoConfiguration {

	@Bean("realtimeStatisticalWriter")
	public StatisticalWriter realtimeStatisticalWriter() {
		return new RealtimeStatisticalWriter();
	}

	@Bean("bulkStatisticalWriter")
	public StatisticalWriter bulkStatisticalWriter() {
		return new BulkStatisticalWriter();
	}

	@ConditionalOnMissingBean
	@Bean
	public TransportClient transportClient(@Value("${spring.application.cluster.jellyfish.brokerUrl}") String brokerUrl) {
		return new HttpTransportClient(brokerUrl);
	}

	@ConditionalOnMissingBean
	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler taskScheduler() {
		final int nThreads = Runtime.getRuntime().availableProcessors() * 2;
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(nThreads);
		threadPoolTaskScheduler.setThreadFactory(new PooledThreadFactory("cooper-task-scheduler-"));
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
		return threadPoolTaskScheduler;
	}

}
