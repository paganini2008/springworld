package com.github.paganini2008.springdessert.cluster.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
	private final Permit permit;
	private final Snapshot snapshot;

	public StatisticMetric(String provider, String path, int maxPermits) {
		this.provider = provider;
		this.path = path;
		this.permit = new Permit(maxPermits);
		this.snapshot = new Snapshot(totalExecution);
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
	public Permit getPermit() {
		return permit;
	}

	@Override
	public Snapshot getSnapshot() {
		return snapshot;
	}

	@Override
	public Map<String, Object> toMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Permit {

		private final AtomicInteger counter;
		private final int maxPermits;

		Permit(int maxPermits) {
			this.counter = new AtomicInteger(0);
			this.maxPermits = maxPermits;
		}

		public int accquire() {
			return counter.incrementAndGet();
		}

		public int accquire(int permits) {
			return counter.addAndGet(permits);
		}

		public int release() {
			return counter.decrementAndGet();
		}

		public int release(int permits) {
			return counter.addAndGet(-permits);
		}

		public int availablePermits() {
			return maxPermits - counter.get();
		}

		public int maxPermits() {
			return maxPermits;
		}

	}

	public static class Snapshot {

		private final List<Request> latestRequests = new LruList<Request>(120);
		private final AtomicLongSequence totalRequestTime = new AtomicLongSequence();
		private volatile long maximumRequestTime;
		private volatile long minimumRequestTime = Long.MAX_VALUE;

		Snapshot(AtomicLongSequence totalExecution) {
			this.totalExecution = totalExecution;
		}

		private final AtomicLongSequence totalExecution;

		public long addRequest(Request request) {
			latestRequests.add(request);
			long elapsed = System.currentTimeMillis() - request.getTimestamp();
			totalRequestTime.addAndGet(elapsed);
			maximumRequestTime = Long.max(maximumRequestTime, elapsed);
			minimumRequestTime = Long.min(minimumRequestTime, elapsed);
			return elapsed;
		}

		public long getMinimumRequestTime() {
			return minimumRequestTime;
		}

		public long getMaximumRequestTime() {
			return maximumRequestTime;
		}

		public long getAverageRequestTime() {
			return totalRequestTime.get() / totalExecution.get();
		}

		public List<Request> getLatestRequests() {
			return new ArrayList<Request>(latestRequests);
		}

	}

}
