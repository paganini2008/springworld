package com.github.paganini2008.springworld.cluster;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfig;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastConfig;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastController;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastEventHandlerBeanProcessor;
import com.github.paganini2008.springworld.cluster.pool.ProcessPoolConfig;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * ContextClusterAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ ContextClusterSupportConfig.class, ContextClusterConfig.class, ContextMulticastConfig.class,
		ContextMulticastEventHandlerBeanProcessor.class, ProcessPoolConfig.class, ContextClusterController.class,
		ContextMulticastController.class, ApplicationContextUtils.class, ConsistencyRequestConfig.class })
public class ContextClusterAutoConfiguration {
}
