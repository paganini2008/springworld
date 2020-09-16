package com.github.paganini2008.springworld.cronkeeper.ui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springworld.cronkeeper.JobAdmin;
import com.github.paganini2008.springworld.cronkeeper.JobManager;
import com.github.paganini2008.springworld.cronkeeper.server.ClusterRestTemplate;
import com.github.paganini2008.springworld.cronkeeper.server.RestJobManager;
import com.github.paganini2008.springworld.cronkeeper.server.ServerModeJobAdmin;

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
@ConditionalOnProperty(name = "spring.application.cluster.scheduler.deployMode", havingValue = "ui")
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
