package com.github.paganini2008.springdessert.cooper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.xtransport.HttpTransportClient;
import com.github.paganini2008.xtransport.TransportClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CooperAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
@ConditionalOnWebApplication
@Configuration
public class CooperAutoConfiguration implements WebMvcConfigurer {

	@Value("#{'${spring.application.cluster.cooper.excludedUrlPatterns:}'.split(',')}")
	private List<String> excludedUrlPatterns = new ArrayList<String>();

	@ConditionalOnMissingBean
	@Bean
	public PathMatchedMap pathMatchedMap() {
		return new PathMatchedMap();
	}

	@Bean("realtimeStatisticalWriter")
	public StatisticalWriter realtimeStatisticalWriter() {
		log.info("Load RealtimeStatisticalWriter");
		return new RealtimeStatisticalWriter();
	}

	@Bean("bulkStatisticalWriter")
	public StatisticalWriter bulkStatisticalWriter() {
		log.info("Load BulkStatisticalWriter");
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

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		InterceptorRegistration interceptorRegistration = registry.addInterceptor(realtimeStatisticalWriter()).addPathPatterns("/**");
		if (CollectionUtils.isNotEmpty(excludedUrlPatterns)) {
			interceptorRegistration.excludePathPatterns(excludedUrlPatterns);
		}
		interceptorRegistration = registry.addInterceptor(bulkStatisticalWriter()).addPathPatterns("/**");
		if (CollectionUtils.isNotEmpty(excludedUrlPatterns)) {
			interceptorRegistration.excludePathPatterns(excludedUrlPatterns);
		}
	}

}
