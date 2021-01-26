package com.github.paganini2008.springdessert.jellyfish.stat;

import com.github.paganini2008.devtools.primitives.Doubles;
import com.github.paganini2008.xtransport.Tuple;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * PathStatistical
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
@ToString
public class PathStatistic {

	private String clusterName;
	private String applicationName;
	private String host;
	private String path;

	public PathStatistic(String clusterName, String applicationName, String host, String path) {
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.host = host;
		this.path = path;
	}

	public PathStatistic() {
	}

	private long totalExecutionCount;
	private long timeoutExecutionCount;
	private long failedExecutionCount;

	public double getTimeoutExectionRatio() {
		double value = totalExecutionCount < 0 ? (double) timeoutExecutionCount / totalExecutionCount : 0;
		return Doubles.toFixed(value, 4);
	}

	public double getFailedExectionRatio() {
		double value = failedExecutionCount < 0 ? (double) failedExecutionCount / totalExecutionCount : 0;
		return Doubles.toFixed(value, 4);
	}

	public static PathStatistic of(Tuple tuple) {
		String clusterName = tuple.getField("clusterName", String.class);
		String applicationName = tuple.getField("applicationName", String.class);
		String host = tuple.getField("host", String.class);
		String path = tuple.getField("path", String.class);
		return new PathStatistic(clusterName, applicationName, host, path);
	}

}
