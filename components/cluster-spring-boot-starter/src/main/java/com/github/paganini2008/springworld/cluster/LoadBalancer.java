package com.github.paganini2008.springworld.cluster;

import java.util.List;

/**
 * 
 * LoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LoadBalancer<T> {

	T select(Object message, List<T> candidates);

}
