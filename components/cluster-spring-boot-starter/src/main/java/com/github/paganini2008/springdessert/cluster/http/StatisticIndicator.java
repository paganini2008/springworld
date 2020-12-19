package com.github.paganini2008.springdessert.cluster.http;

import java.util.Map;

/**
 * 
 * StatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface StatisticIndicator {

	Statistic getStatistic(String provider, Request request);
	
	Map<String, Statistic> getAll(String provider);

}