package com.github.paganini2008.springdessert.jobsoup.ui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springdessert.jobsoup.JobAdmin;
import com.github.paganini2008.springdessert.jobsoup.JobManager;
import com.github.paganini2008.springdessert.jobsoup.server.ClusterRestTemplate;
import com.github.paganini2008.springdessert.jobsoup.server.RestJobManager;
import com.github.paganini2008.springdessert.jobsoup.server.ServerModeJobAdmin;

/**
 * 
 * UIModeConfiguration
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@ConditionalOnWebApplication
@Configuration
public class UIModeConfiguration {

	@Bean
	public ClusterRestTemplate clusterRestTemplate() {
		return new UIModeClusterRestTemplate();
	}

	@Bean
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new RestJobManager();
	}

	@Bean
	public JobAdmin jobAdmin() {
		return new ServerModeJobAdmin();
	}
}
