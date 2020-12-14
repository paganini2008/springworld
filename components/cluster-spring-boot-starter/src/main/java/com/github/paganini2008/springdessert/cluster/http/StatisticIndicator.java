package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * StatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface StatisticIndicator {

	Statistic getStatistic(String provider, String path);

}