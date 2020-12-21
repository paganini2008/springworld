package com.github.paganini2008.springdessert.cluster.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * AbstractStatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class AbstractStatisticIndicator implements StatisticIndicator, Runnable {

	protected final MultiMappedMap<String, String, Statistic> cache = new MultiMappedMap<String, String, Statistic>();
	private final PathMatcher pathMatcher = new AntPathMatcher();
	private final List<String> pathPatterns = new CopyOnWriteArrayList<String>();

	@Autowired
	public void configure(TaskScheduler taskScheduler) {
		taskScheduler.scheduleWithFixedDelay(this, Duration.ofSeconds(1));
	}

	public void includePath(String... pathPatterns) {
		if (ArrayUtils.isNotEmpty(pathPatterns)) {
			this.pathPatterns.addAll(Arrays.asList(pathPatterns));
		}
	}

	@Override
	public Statistic compute(String provider, Request request) {
		String path;
		if (pathPatterns.isEmpty() || shouldCompute(provider, request)) {
			path = request.getPath();
		} else {
			path = "/**";
		}
		Statistic statistic = cache.get(provider, path);
		if (statistic == null) {
			cache.putIfAbsent(provider, path, new Statistic(provider, path, ((ForwardedRequest) request).getAllowedPermits()));
			statistic = cache.get(provider, path);
		}
		return statistic;
	}

	protected boolean shouldCompute(String provider, Request request) {
		for (String pathPattern : pathPatterns) {
			if (pathMatcher.match(pathPattern, request.getPath())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Statistic> toCollection(String provider) {
		return cache.containsKey(provider) ? Collections.unmodifiableCollection((cache.get(provider).values())) : Collections.EMPTY_LIST;
	}

	@Override
	public Map<String, Collection<Statistic>> toMap() {
		Map<String, Collection<Statistic>> copy = new HashMap<String, Collection<Statistic>>();
		for (Map.Entry<String, Map<String, Statistic>> entry : cache.entrySet()) {
			copy.put(entry.getKey(), Collections.unmodifiableCollection(entry.getValue().values()));
		}
		return copy;
	}

	@Override
	public void run() {
		for (Map.Entry<String, Map<String, Statistic>> outter : cache.entrySet()) {
			for (Map.Entry<String, Statistic> inner : outter.getValue().entrySet()) {
				inner.getValue().calculateQps();
			}
		}
	}

}
