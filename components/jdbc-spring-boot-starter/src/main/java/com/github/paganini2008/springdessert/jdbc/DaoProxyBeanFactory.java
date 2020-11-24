package com.github.paganini2008.springdessert.jdbc;

import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * DaoProxyBeanFactory
 *
 * @author Fred Feng
 * @since 1.0
 */
public class DaoProxyBeanFactory<T> implements FactoryBean<T> {

	private final Class<T> interfaceClass;

	public DaoProxyBeanFactory(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Autowired
	private DataSource dataSource;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new DaoProxyBean<T>(dataSource, interfaceClass));
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
