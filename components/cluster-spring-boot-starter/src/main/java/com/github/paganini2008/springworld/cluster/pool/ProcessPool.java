package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ProcessPool
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ProcessPool {

	void execute(String beanName, Class<?> beanClass, String methodName, Object... arguments);

	Promise submit(String beanName, Class<?> beanClass, String methodName, Object... arguments);

	void shutdown();

}
