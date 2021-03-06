package com.github.paganini2008.springdessert.jellyfish.stat;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.MetricUnits;
import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;
import com.github.paganini2008.springdessert.xtransport.Handler;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * BulkStatisticHandler
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class BulkStatisticHandler implements Handler {

	@Autowired
	private TransientStatisticSynchronizer statisticSynchronizer;

	@Override
	public void onData(Tuple tuple) {
		long timestamp = tuple.getTimestamp();
		long totalExecutionCount = tuple.getField("totalExecutionCount", Long.class);
		long timeoutExecutionCount = tuple.getField("timeoutExecutionCount", Long.class);
		long failedExecutionCount = tuple.getField("failedExecutionCount", Long.class);

		PathSummary pathStatistic = statisticSynchronizer.getPathSummary(Catalog.of(tuple));
		pathStatistic.setTotalExecutionCount(totalExecutionCount);
		pathStatistic.setTimeoutExecutionCount(timeoutExecutionCount);
		pathStatistic.setFailedExecutionCount(failedExecutionCount);

		int qps = tuple.getField("qps", Integer.class);
		SequentialMetricsCollector sequentialMetricsCollector = statisticSynchronizer.getMetricsCollector(Catalog.of(tuple));
		sequentialMetricsCollector.set("qps", timestamp, MetricUnits.valueOf(qps));
	}

	@Override
	public String getTopic() {
		return "com.github.paganini2008.springdessert.cooper.BulkStatisticalWriter";
	}

}
