package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.RecursiveTask;

/**
 * 
 * MultiProcessingRecursiveTask
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class MultiProcessingRecursiveTask<T> extends RecursiveTask<T> {

	private static final long serialVersionUID = 1L;

	protected abstract T compute();

	public T call(Object... arguments) {
		return null;
	}

}
