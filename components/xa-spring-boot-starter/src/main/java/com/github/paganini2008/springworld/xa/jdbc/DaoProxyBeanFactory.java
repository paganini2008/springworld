package com.github.paganini2008.springworld.xa.jdbc;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * DaoProxyBeanFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class DaoProxyBeanFactory<T> implements FactoryBean<T> {

	private final Class<T> interfaceClass;

	public DaoProxyBeanFactory(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Autowired
	private JdbcOperationsHolder jdbcOperationsHolder;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new DaoProxyBean<T>(interfaceClass, jdbcOperationsHolder));
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
