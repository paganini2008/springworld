package com.github.paganini2008.springdessert.jellyfish.stat;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;
import com.github.paganini2008.springdessert.xtransport.Handler;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * RealtimeStatisticalHandler
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RealtimeStatisticalHandler implements Handler {

	@Autowired
	private TransientStatisticalContext transientStatisticalContext;

	@Override
	public void onData(Tuple tuple) {
		long elapsed = tuple.getField("elapsed", Long.class);
		long concurrency = tuple.getField("concurrency", Long.class);
		long timestamp = tuple.getField("requestTime", Long.class);
		SequentialMetricsCollector sequentialMetricsCollector = transientStatisticalContext
				.getMetricsCollector(MetricCollectorKey.of(tuple));
		sequentialMetricsCollector.set("elapsed", timestamp, new StatisticalLongMetricUnit(tuple, elapsed));
		sequentialMetricsCollector.set("concurrency", timestamp, new StatisticalLongMetricUnit(tuple, concurrency));
	}

	@Override
	public String getTopic() {
		return "com.github.paganini2008.springdessert.logstat.RealtimeStatisticalWriter";
	}

}
