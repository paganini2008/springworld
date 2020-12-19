package com.github.paganini2008.springdessert.cluster.http;

import java.util.Map;

import com.github.paganini2008.springdessert.cluster.http.StatisticMetric.Permit;
import com.github.paganini2008.springdessert.cluster.http.StatisticMetric.Snapshot;

/**
 * 
 * Statistic
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface Statistic {

	String getProvider();

	String getPath();

	long getTotalExecutionCount();

	long getTimeoutExecutionCount();

	long getFailedExecutionCount();

	Permit getPermit();

	Snapshot getSnapshot();
	
	Map<String, Object> toMap();

}