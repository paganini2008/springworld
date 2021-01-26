package com.github.paganini2008.springdessert.jellyfish.stat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * MetricVO
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
@ToString
public class MetricVO {

	private String clusterName;
	private String applicationName;
	private String host;
	private String path;
	private long highestValue;
	private long lowestValue;
	private long totalValue;
	private int count;
	private double middleValue;
	private long timestamp;

}
