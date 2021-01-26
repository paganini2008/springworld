package com.github.paganini2008.springdessert.jellyfish.stat;

import com.github.paganini2008.xtransport.Tuple;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * Catalog
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@ToString
@EqualsAndHashCode
public class Catalog {

	private String clusterName;
	private String applicationName;
	private String host;
	private String path;

	public Catalog(String clusterName, String applicationName, String host, String path) {
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.host = host;
		this.path = path;
	}

	public Catalog() {
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

	public static Catalog of(Tuple tuple) {
		String clusterName = tuple.getField("clusterName", String.class);
		String applicationName = tuple.getField("applicationName", String.class);
		String host = tuple.getField("host", String.class);
		String path = tuple.getField("path", String.class);
		return new Catalog(clusterName, applicationName, host, path);
	}

}
