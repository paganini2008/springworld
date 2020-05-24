package com.github.paganini2008.springworld.cluster;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfig;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastConfig;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastController;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListenerBeanProcessor;
import com.github.paganini2008.springworld.cluster.pool.ProcessPoolConfig;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * ApplicationClusterAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ ApplicationClusterSupportConfig.class, ApplicationClusterConfig.class, ClusterMulticastConfig.class,
		ClusterMulticastEventListenerBeanProcessor.class, ProcessPoolConfig.class, ApplicationClusterController.class,
		ClusterMulticastController.class, ApplicationContextUtils.class, ConsistencyRequestConfig.class })
public class ApplicationClusterAutoConfiguration {
}
