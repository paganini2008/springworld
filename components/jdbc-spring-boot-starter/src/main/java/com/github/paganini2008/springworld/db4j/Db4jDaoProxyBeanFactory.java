package com.github.paganini2008.springworld.db4j;

import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Db4jDaoProxyBeanFactory
 *
 * @author Fred Feng
 * @since 1.0
 */
public class Db4jDaoProxyBeanFactory<T> implements FactoryBean<T> {

	private final Class<T> interfaceClass;

	public Db4jDaoProxyBeanFactory(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Autowired
	private DataSource dataSource;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new Db4jDaoProxyBean<T>(dataSource, interfaceClass));
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
