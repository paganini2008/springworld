package com.github.paganini2008.springworld.cluster.utils;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * BeanLifeCycle
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface BeanLifeCycle extends InitializingBean, DisposableBean {

	default void configure() throws Exception {
	}

	@Override
	default void destroy() {
	}

	@Override
	default void afterPropertiesSet() throws Exception {
		configure();
	}

}
