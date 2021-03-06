package com.github.paganini2008.springdessert.fastjpa.support;

import com.github.paganini2008.devtools.collection.Tuple;

/**
 * 
 * BeanPropertyRowMapper
 * 
 * @author Jimmy Hoff
 * 
 */
public class BeanPropertyRowMapper<T> implements RowMapper<T> {

	private final BeanReflection<T> beanReflection;

	public BeanPropertyRowMapper(Class<T> resultClass, String... includedProperties) {
		this.beanReflection = new BeanReflection<T>(resultClass, includedProperties);
	}

	public T mapRow(Tuple tuple) {
		T instance = beanReflection.instantiateBean();
		for (String propertyName : tuple.keys()) {
			beanReflection.setProperty(instance, propertyName, tuple.get(propertyName));
		}
		return instance;
	}
}
