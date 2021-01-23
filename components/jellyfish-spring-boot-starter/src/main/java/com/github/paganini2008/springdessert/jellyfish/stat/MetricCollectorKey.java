package com.github.paganini2008.springdessert.jellyfish.stat;

import com.github.paganini2008.xtransport.Tuple;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * MetricCollectorKey
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@ToString
@EqualsAndHashCode
public class MetricCollectorKey {

	private final String clusterName;
	private final String applicationName;
	private final String host;
	private final String path;

	public MetricCollectorKey(String clusterName, String applicationName, String host, String path) {
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.host = host;
		this.path = path;
	}

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

	public static MetricCollectorKey of(Tuple tuple) {
		String clusterName = tuple.getField("clusterName", String.class);
		String applicationName = tuple.getField("applicationName", String.class);
		String host = tuple.getField("host", String.class);
		String path = tuple.getField("path", String.class);
		return new MetricCollectorKey(clusterName, applicationName, host, path);
	}

}
