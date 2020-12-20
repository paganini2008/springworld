package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;
import java.util.Map;

/**
 * 
 * StatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface StatisticIndicator {

	Statistic compute(String provider, Request request);

	List<Statistic> list(String provider);

	Map<String, List<Statistic>> toMap();

}