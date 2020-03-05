package com.github.paganini2008.springworld.xa.jdbc;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.xa.XaTransactionManager;

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
	private XaTransactionManager transactionManager;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new DaoProxyBean<T>(interfaceClass, transactionManager));
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
