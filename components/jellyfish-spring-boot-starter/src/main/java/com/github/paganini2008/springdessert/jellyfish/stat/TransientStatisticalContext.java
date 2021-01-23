package com.github.paganini2008.springdessert.jellyfish.stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;
import com.github.paganini2008.devtools.collection.SimpleSequentialMetricsCollector;
import com.github.paganini2008.devtools.date.SpanUnit;

/**
 * 
 * TransientStatisticalContext
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class TransientStatisticalContext {

	private final Map<MetricCollectorKey, PathStatistic> totalStatistic = new ConcurrentHashMap<MetricCollectorKey, PathStatistic>();
	private final Map<MetricCollectorKey, SequentialMetricsCollector> realtimeCollectors = new ConcurrentHashMap<MetricCollectorKey, SequentialMetricsCollector>();

	public SequentialMetricsCollector getMetricsCollector(MetricCollectorKey collectorKey) {
		return MapUtils.get(realtimeCollectors, collectorKey, () -> {
			return new SimpleSequentialMetricsCollector(60, 1, SpanUnit.MINUTE, null);
		});
	}

	public PathStatistic getPathStatistic(MetricCollectorKey collectorKey) {
		return MapUtils.get(totalStatistic, collectorKey, () -> {
			return new PathStatistic(collectorKey.getClusterName(), collectorKey.getApplicationName(), collectorKey.getHost(),
					collectorKey.getPath());
		});
	}
}
