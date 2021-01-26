package com.github.paganini2008.springdessert.jellyfish.stat;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.MetricUnits;
import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;
import com.github.paganini2008.springdessert.xtransport.Handler;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * RealtimeStatisticHandler
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RealtimeStatisticHandler implements Handler {

	@Autowired
	private TransientStatisticSynchronizer transientStatisticalContext;

	@Override
	public void onData(Tuple tuple) {
		long elapsed = tuple.getField("elapsed", Long.class);
		long concurrency = tuple.getField("concurrency", Long.class);
		long timestamp = tuple.getField("requestTime", Long.class);
		SequentialMetricsCollector sequentialMetricsCollector = transientStatisticalContext.getMetricsCollector(Catalog.of(tuple));
		sequentialMetricsCollector.set("rt", timestamp, MetricUnits.valueOf(elapsed));
		sequentialMetricsCollector.set("cons", timestamp, MetricUnits.valueOf(concurrency));
	}

	@Override
	public String getTopic() {
		return "com.github.paganini2008.springdessert.cooper.RealtimeStatisticalWriter";
	}

}
