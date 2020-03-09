package com.github.paganini2008.springworld.jdbcplus;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * SpringDaoProxyBeanFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class SpringDaoProxyBeanFactory<T> implements FactoryBean<T> {

	private final Class<T> interfaceClass;

	public SpringDaoProxyBeanFactory(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Autowired
	private EnhancedJdbcTemplate jdbcTemplate;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new SpringDaoProxyBean<T>(interfaceClass, jdbcTemplate));
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
