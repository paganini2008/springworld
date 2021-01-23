package com.github.paganini2008.springdessert.jellyfish.stat;

import com.github.paganini2008.devtools.collection.MetricUnits.LongMetricUnit;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * StatisticalLongMetricUnit
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class StatisticalLongMetricUnit extends LongMetricUnit {

	private final String clusterName;
	private final String applicationName;
	private final String host;
	private final String path;

	StatisticalLongMetricUnit(Tuple tuple, long value) {
		super(value);
		
		this.clusterName = tuple.getField("clusterName", String.class);
		this.applicationName = tuple.getField("applicationName", String.class);
		this.host = tuple.getField("host", String.class);
		this.path = tuple.getField("path", String.class);
		this.timestamp = tuple.getTimestamp();
	}

	private long timestamp;

	public String getClusterName() {
		return clusterName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
