package com.github.paganini2008.springdessert.jellyfish.stat;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MetricUnit;
import com.github.paganini2008.devtools.collection.MultiMappedMap;
import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TransientStatisticSynchronizer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class TransientStatisticSynchronizer implements Runnable, InitializingBean {

	public static final String KEY_PATTERN_TOTAL_SUMMARY = "jellyfish:index:%s";
	public static final String KEY_PATTERN_REALTIME_SUMMARY = "jellyfish:%s:%s";
	private final Map<Catalog, PathSummary> totalSummary = new ConcurrentHashMap<Catalog, PathSummary>();
	private final Map<Catalog, SequentialMetricsCollector> realtimeCollectors = new ConcurrentHashMap<Catalog, SequentialMetricsCollector>();

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private MetricsCollectorCustomizer metricsCollectorCustomizer;

	private final Map<Catalog, Cache> totalSummaryCaching = new ConcurrentHashMap<Catalog, Cache>();
	private final MultiMappedMap<Catalog, String, Cache> realtimeSummaryCaching = new MultiMappedMap<Catalog, String, Cache>();

	public SequentialMetricsCollector getMetricsCollector(Catalog catalog) {
		return MapUtils.get(realtimeCollectors, catalog, () -> {
			return metricsCollectorCustomizer.createSequentialMetricsCollector(catalog);
		});
	}

	public PathSummary getPathSummary(Catalog catalog) {
		return MapUtils.get(totalSummary, catalog, () -> {
			return new PathSummary(catalog.getClusterName(), catalog.getApplicationName(), catalog.getHost(), catalog.getPath());
		});
	}

	public Catalog[] getCatalogs() {
		return totalSummary.keySet().toArray(new Catalog[0]);
	}

	public Cache getTotalSummaryCache(Catalog catalog) {
		return totalSummaryCaching.get(catalog);
	}

	public Cache getRealtimeSummaryCache(Catalog catalog, String metric) {
		return realtimeSummaryCaching.get(catalog, metric);
	}

	@Override
	public void run() {
		if (totalSummary.size() > 0) {
			for (Map.Entry<Catalog, PathSummary> entry : totalSummary.entrySet()) {
				final Catalog catalog = entry.getKey();
				PathSummary pathSummary = entry.getValue();
				Cache cache = MapUtils.get(totalSummaryCaching, catalog, () -> {
					String key = String.format(KEY_PATTERN_TOTAL_SUMMARY, catalog.getIdentifier());
					return new MetricCache(key, redisTemplate);
				});
				cache.putObject("totalExecutionCount", pathSummary.getTotalExecutionCount());
				cache.putObject("successExecutionCount", pathSummary.getSuccessExecutionCount());
				cache.putObject("failedExecutionCount", pathSummary.getFailedExecutionCount());
				cache.putObject("timeoutExecutionCount", pathSummary.getTimeoutExecutionCount());
			}
			log.info("Sync {} path summary.", totalSummary.size());
		}

		if (realtimeCollectors.size() > 0) {
			SequentialMetricsCollector metricsCollector;
			for (Map.Entry<Catalog, SequentialMetricsCollector> entry : realtimeCollectors.entrySet()) {
				final Catalog catalog = entry.getKey();
				metricsCollector = entry.getValue();
				sync(catalog, metricsCollector, "rt");
				sync(catalog, metricsCollector, "cons");
				sync(catalog, metricsCollector, "qps");
			}
		}
	}

	private void sync(Catalog catalog, SequentialMetricsCollector metricsCollector, String metric) {
		Map<String, MetricUnit> metricUnits = metricsCollector.sequence(metric);
		Cache cache = realtimeSummaryCaching.getIfNecessary(catalog, metric, () -> {
			String key = String.format(KEY_PATTERN_REALTIME_SUMMARY, metric, catalog.getIdentifier());
			return new MetricCache(key, redisTemplate).sortedCache(metricsCollectorCustomizer.getBufferSize(), true, null);
		});
		MetricUnit metricUnit;
		for (Map.Entry<String, MetricUnit> entry : metricUnits.entrySet()) {
			metricUnit = entry.getValue();
			Metric vo = new Metric();
			vo.setClusterName(catalog.getClusterName());
			vo.setApplicationName(catalog.getApplicationName());
			vo.setHost(catalog.getHost());
			vo.setPath(catalog.getPath());
			vo.setHighestValue(metricUnit.getHighestValue().longValue());
			vo.setLowestValue(metricUnit.getLowestValue().longValue());
			vo.setTotalValue(metricUnit.getTotalValue().longValue());
			vo.setMiddleValue(metricUnit.getMiddleValue(0).longValue());
			vo.setCount(metricUnit.getCount());
			vo.setTimestamp(metricUnit.getTimestamp());
			if (metricUnit instanceof RealtimeMetricUnit) {
				RealtimeMetricUnit realtimeMetricUnit = (RealtimeMetricUnit) metricUnit;
				vo.setFailedCount(realtimeMetricUnit.getFailedCount());
				vo.setTimeoutCount(realtimeMetricUnit.getTimeoutCount());
				vo.setSuccessCount(vo.getCount() - vo.getFailedCount() - vo.getTimeoutCount());
			}
			cache.putObject(entry.getKey(), vo);
		}
		log.info("Sync {} path metric summary", cache.getSize());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		taskScheduler.scheduleWithFixedDelay(this, Duration.ofSeconds(3));
		log.info("Start TransientStatisticSynchronizer.");
	}
}
