package com.github.paganini2008.springworld.cluster.pool;

import java.util.Collection;

/**
 * 
 * DefaultCallParallelization
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DefaultCallParallelization implements CallParallelization {

	@Override
	public Object[] slice(Object argument) {
		if (argument instanceof CharSequence) {
			return ((CharSequence) argument).toString().split(",");
		} else if (argument instanceof Object[]) {
			return (Object[]) argument;
		} else if (argument instanceof Collection<?>) {
			return ((Collection<?>) argument).toArray();
		}
		return new Object[] { argument };
	}

	@Override
	public Object merge(Object[] results) {
		return results;
	}

}
