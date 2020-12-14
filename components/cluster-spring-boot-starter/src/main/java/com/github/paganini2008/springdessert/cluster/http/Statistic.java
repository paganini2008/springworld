package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

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

	Snapshot getSnapshot();

	interface Snapshot {
		
		long addRequest(Request request);

		long getMaximumRequestTime();

		long getAverageRequestTime();
		
		long getMinimumRequestTime();

		List<Request> getLatestRequests();

	}

}