package com.github.paganini2008.springdessert.jellyfish.stat;

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

	private final String clusterName;
	private final String applicationName;
	private final String host;
	private final String path;

	public PathStatistic(String clusterName, String applicationName, String host, String path) {
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.host = host;
		this.path = path;
	}

	private long totalExecutionCount;
	private long timeoutExecutionCount;
	private long failedExecutionCount;

	public static PathStatistic of(Tuple tuple) {
		String clusterName = tuple.getField("clusterName", String.class);
		String applicationName = tuple.getField("applicationName", String.class);
		String host = tuple.getField("host", String.class);
		String path = tuple.getField("path", String.class);
		return new PathStatistic(clusterName, applicationName, host, path);
	}

}
