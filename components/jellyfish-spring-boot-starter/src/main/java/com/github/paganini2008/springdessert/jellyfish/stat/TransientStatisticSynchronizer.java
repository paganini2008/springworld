package com.github.paganini2008.springdessert.jellyfish.stat;

import static com.github.paganini2008.springdessert.jellyfish.Utils.encodeString;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MetricUnit;
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

	public static final String KEY_TOTAL_SUMMARY = "jellyfish:%s:%s:%s:%s";
	public static final String KEY_REALTIME_SUMMARY = "jellyfish:%s:%s:%s:%s:%s";
	private final Map<Catalog, PathStatistic> totalStatistic = new ConcurrentHashMap<Catalog, PathStatistic>();
	private final Map<Catalog, SequentialMetricsCollector> realtimeCollectors = new ConcurrentHashMap<Catalog, SequentialMetricsCollector>();

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private SequentialMetricsCollectorFactory sequentialMetricsCollectorFactory;

	public SequentialMetricsCollector getMetricsCollector(Catalog catalog) {
		return MapUtils.get(realtimeCollectors, catalog, () -> {
			return sequentialMetricsCollectorFactory.createSequentialMetricsCollector(catalog);
		});
	}

	public PathStatistic getPathStatistic(Catalog catalog) {
		return MapUtils.get(totalStatistic, catalog, () -> {
			return new PathStatistic(catalog.getClusterName(), catalog.getApplicationName(), catalog.getHost(), catalog.getPath());
		});
	}

	public Catalog[] getCatalogs() {
		return totalStatistic.keySet().toArray(new Catalog[0]);
	}

	@Override
	public void run() {
		String key;
		Catalog catalog;
		PathStatistic pathStatistic;
		if (totalStatistic.size() > 0) {
			for (Map.Entry<Catalog, PathStatistic> entry : totalStatistic.entrySet()) {
				catalog = entry.getKey();
				pathStatistic = entry.getValue();
				key = String.format(KEY_TOTAL_SUMMARY, catalog.getClusterName(), catalog.getApplicationName(),
						encodeString(catalog.getHost()), encodeString(catalog.getPath()));
				redisTemplate.opsForHash().put(key, "totalExecutionCount", pathStatistic.getTotalExecutionCount());
				redisTemplate.opsForHash().put(key, "failedExecutionCount", pathStatistic.getFailedExecutionCount());
				redisTemplate.opsForHash().put(key, "timeoutExecutionCount", pathStatistic.getTimeoutExecutionCount());
				redisTemplate.opsForHash().put(key, "failedExecutionRatio", pathStatistic.getFailedExectionRatio());
				redisTemplate.opsForHash().put(key, "timeoutExecutionRatio", pathStatistic.getTimeoutExectionRatio());
			}
			log.info("Sync {} path statistic info.", totalStatistic.size());
		}

		if (realtimeCollectors.size() > 0) {
			SequentialMetricsCollector metricsCollector;
			for (Map.Entry<Catalog, SequentialMetricsCollector> entry : realtimeCollectors.entrySet()) {
				catalog = entry.getKey();
				metricsCollector = entry.getValue();
				sync(catalog, metricsCollector, "rt");
				sync(catalog, metricsCollector, "cons");
				sync(catalog, metricsCollector, "qps");
			}
		}
	}

	private void sync(Catalog catalog, SequentialMetricsCollector metricsCollector, String metric) {
		final String key = String.format(KEY_REALTIME_SUMMARY, metric, catalog.getClusterName(), catalog.getApplicationName(),
				encodeString(catalog.getHost()), encodeString(catalog.getPath()));
		redisTemplate.delete(key);

		Map<String, MetricUnit> metricUnits = metricsCollector.sequence(metric);
		MetricUnit metricUnit;
		for (Map.Entry<String, MetricUnit> entry : metricUnits.entrySet()) {
			metricUnit = entry.getValue();
			MetricVO vo = new MetricVO();
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
			redisTemplate.opsForHash().put(key, entry.getKey(), vo);
		}
		log.info("Sync {} metric units", redisTemplate.opsForHash().size(key));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		taskScheduler.scheduleWithFixedDelay(this, Duration.ofSeconds(3));
		log.info("Start TransientStatisticSynchronizer.");
	}
}
