package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.paganini2008.devtools.collection.LruList;
import com.github.paganini2008.devtools.multithreads.AtomicLongSequence;

/**
 * 
 * StatisticMetric
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class StatisticMetric implements Statistic {

	private final String provider;
	private final String path;
	private final AtomicLongSequence totalExecution = new AtomicLongSequence();
	private final AtomicLongSequence timeoutExecution = new AtomicLongSequence();
	private final AtomicLongSequence failedExecution = new AtomicLongSequence();
	private final SnapshotImpl snapshot = new SnapshotImpl();

	StatisticMetric(String provider, String path) {
		this.provider = provider;
		this.path = path;
	}

	@Override
	public String getProvider() {
		return provider;
	}

	@Override
	public String getPath() {
		return path;
	}

	@JsonIgnore
	public AtomicLongSequence getTotalExecution() {
		return totalExecution;
	}

	@JsonIgnore
	public AtomicLongSequence getTimeoutExecution() {
		return timeoutExecution;
	}

	@JsonIgnore
	public AtomicLongSequence getFailedExecution() {
		return failedExecution;
	}

	@Override
	public long getTotalExecutionCount() {
		return totalExecution.get();
	}

	@Override
	public long getTimeoutExecutionCount() {
		return timeoutExecution.get();
	}

	@Override
	public long getFailedExecutionCount() {
		return failedExecution.get();
	}

	@Override
	public Snapshot getSnapshot() {
		return snapshot;
	}

	public class SnapshotImpl implements Snapshot {

		private final List<Request> latestRequests = new LruList<Request>(120);
		private final AtomicLongSequence totalRequestTime = new AtomicLongSequence();
		private volatile long maximumRequestTime;
		private volatile long minimumRequestTime = Long.MAX_VALUE;

		public long addRequest(Request request) {
			latestRequests.add(request);
			long elapsed = System.currentTimeMillis() - request.getTimestamp();
			totalRequestTime.addAndGet(elapsed);
			maximumRequestTime = Long.max(maximumRequestTime, elapsed);
			minimumRequestTime = Long.min(minimumRequestTime, elapsed);
			return elapsed;
		}

		@Override
		public long getMinimumRequestTime() {
			return minimumRequestTime;
		}

		@Override
		public long getMaximumRequestTime() {
			return maximumRequestTime;
		}

		@Override
		public long getAverageRequestTime() {
			return totalRequestTime.get() / totalExecution.get();
		}

		@Override
		public List<Request> getLatestRequests() {
			return latestRequests;
		}

	}

}
