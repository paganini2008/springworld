package com.github.paganini2008.springdessert.cluster.utils;

import java.util.List;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * LoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LoadBalancer {

	ApplicationInfo select(Object message, List<ApplicationInfo> candidates);

}
