package com.github.paganini2008.springdessert.cluster;

import java.util.List;

/**
 * 
 * LoadBalancer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface LoadBalancer {

	ApplicationInfo select(Object message, List<ApplicationInfo> candidates);

}
