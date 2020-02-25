package com.github.paganini2008.transport;

import java.util.Map;

/**
 * 
 * Tuple
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Tuple extends Cloneable {

	boolean hasField(String fieldName);

	void setField(String fieldName, Object value);

	Object getField(String fieldName);

	<T> T getField(String fieldName, Class<T> requiredType);

	void fill(Object object);

	Map<String, Object> toMap();

	Tuple clone();

	public static Tuple newTuple() {
		return new TupleImpl();
	}

	public static Tuple byString(String content) {
		Tuple tuple = new TupleImpl();
		tuple.setField("content", content);
		return tuple;
	}

	public static Tuple wrap(Map<String, ?> kwargs) {
		return new TupleImpl(kwargs);
	}

}
