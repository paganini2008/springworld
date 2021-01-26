package com.github.paganini2008.springdessert.jellyfish.stat;

import java.util.Map;

import com.github.paganini2008.devtools.collection.SequentialMetricsCollector;

/**
 * 
 * SequentialMetricsCollectorFactory
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface SequentialMetricsCollectorFactory {

	SequentialMetricsCollector createSequentialMetricsCollector();

	@SuppressWarnings("rawtypes")
	Map render(Map data);

}
