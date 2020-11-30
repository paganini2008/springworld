package com.github.paganini2008.xtransport;

import java.util.Map;

/**
 * 
 * Tuple
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface Tuple {

	static final String DEFAULT_TOPIC = "default";
	static final String KEYWORD_CONTENT = "content";
	static final String KEYWORD_TOPIC = "topic";
	static final Tuple PING = Tuple.byString("PING");
	static final Tuple PONG = Tuple.byString("PONG");

	boolean hasField(String fieldName);

	void setField(String fieldName, Object value);

	Object getField(String fieldName);

	default Object getField(String fieldName, Object defaultValue) {
		Object value;
		if ((value = getField(fieldName)) == null) {
			value = defaultValue;
		}
		return value;
	}

	<T> T getField(String fieldName, Class<T> requiredType);

	void append(Map<String, ?> m);

	void fill(Object object);

	Map<String, Object> toMap();

	Tuple copy();

	default String getTopic() {
		try {
			return (String) getField(KEYWORD_TOPIC, DEFAULT_TOPIC);
		} catch (RuntimeException e) {
			throw new TransportClientException("Don't use topic as key to put into Tuple because it is a keyword.");
		}
	}

	default boolean isPing() {
		return "PING".equals(getField(KEYWORD_CONTENT));
	}

	default boolean isPong() {
		return "PONG".equals(getField(KEYWORD_CONTENT));
	}

	public static Tuple newOne() {
		return new TupleImpl();
	}

	public static Tuple byString(String content) {
		Tuple tuple = new TupleImpl();
		tuple.setField(KEYWORD_CONTENT, content);
		return tuple;
	}

	public static Tuple wrap(Map<String, ?> kwargs) {
		Tuple tuple = new TupleImpl();
		tuple.append(kwargs);
		return tuple;
	}

}
