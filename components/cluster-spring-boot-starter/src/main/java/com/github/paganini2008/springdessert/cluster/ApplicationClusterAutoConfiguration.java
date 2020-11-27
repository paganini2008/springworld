package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequestConfig;
import com.github.paganini2008.springdessert.cluster.http.RestClientConfig;
import com.github.paganini2008.springdessert.cluster.multicast.ClusterMulticastConfig;
import com.github.paganini2008.springdessert.cluster.pool.ProcessPoolConfig;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springdessert.cluster.utils.BeanExpressionUtils;
import com.github.paganini2008.springdessert.cluster.utils.LazilyAutowiredBeanInspector;

/**
 * 
 * ApplicationClusterAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ ApplicationContextUtils.class, 
		BeanExpressionUtils.class, 
		ApplicationClusterConfig.class, 
		ClusterMulticastConfig.class,
		ProcessPoolConfig.class, 
		ConsistencyRequestConfig.class,
		RestClientConfig.class })
public class ApplicationClusterAutoConfiguration {
	
	@Bean
	public LazilyAutowiredBeanInspector lazilyAutowiredBeanInspector() {
		return new LazilyAutowiredBeanInspector();
	}
}
