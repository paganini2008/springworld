package com.github.paganini2008.springworld.jobstorm.ui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springworld.jobstorm.JobAdmin;
import com.github.paganini2008.springworld.jobstorm.JobManager;
import com.github.paganini2008.springworld.jobstorm.server.ClusterRestTemplate;
import com.github.paganini2008.springworld.jobstorm.server.RestJobManager;
import com.github.paganini2008.springworld.jobstorm.server.ServerModeJobAdmin;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * UIModeConfiguration
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@ConditionalOnProperty(name = "jobstorm.deploy.mode", havingValue = "ui")
@Configuration
public class UIModeConfiguration {

	public UIModeConfiguration() {
		log.info("<<<                                                  >>>");
		log.info("<<<                Crontab v2.0-RC4                  >>>");
		log.info("<<<              Current Job Deploy Mode             >>>");
		log.info("<<<                 [UI Mode]                        >>>");
		log.info("<<<                                                  >>>");
	}

	@Bean
	public ClusterRestTemplate clusterRestTemplate() {
		return new UIModeClusterRestTemplate();
	}

	@Bean(initMethod = "configure", destroyMethod = "close")
	@ConditionalOnMissingBean(JobManager.class)
	public JobManager jobManager() {
		return new RestJobManager();
	}

	@Bean
	public JobAdmin jobAdmin() {
		return new ServerModeJobAdmin();
	}
}
