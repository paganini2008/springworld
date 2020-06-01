package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ProcessPool
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface ProcessPool {

	void submit(String beanName, Class<?> beanClass, String methodName, Object... arguments);
	
	void shutdown();

}
