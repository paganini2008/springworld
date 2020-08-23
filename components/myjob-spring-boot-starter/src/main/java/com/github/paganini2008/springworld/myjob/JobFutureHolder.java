package com.github.paganini2008.springworld.myjob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * JobFutureHolder
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobFutureHolder {

	private final Map<JobKey, JobFuture> cache = new ConcurrentHashMap<JobKey, JobFuture>();

	public void add(JobKey jobKey, JobFuture jobFuture) {
		cache.put(jobKey, jobFuture);
	}

	public JobFuture get(JobKey jobKey) {
		return cache.get(jobKey);
	}

	public boolean hasKey(JobKey jobKey) {
		return cache.containsKey(jobKey);
	}

	public void cancel(JobKey jobKey) {
		JobFuture jobFuture = cache.remove(jobKey);
		if (jobFuture != null) {
			jobFuture.cancel();
		}
	}

	public void clear() {
		for (Map.Entry<JobKey, JobFuture> entry : cache.entrySet()) {
			entry.getValue().cancel();
		}
		cache.clear();
	}

	public int size() {
		return cache.size();
	}

}
