package com.github.paganini2008.springdessert.cluster.pool;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * InvocationBarrier
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public final class InvocationBarrier {

	private final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}

	};

	public void setCompleted() {
		System.out.println("CurrentName: "+ThreadUtils.currentThreadName());
		threadLocal.set(Boolean.TRUE);
	}

	public boolean isCompleted() {
		boolean result = threadLocal.get();
		threadLocal.remove();
		return result;
	}

}