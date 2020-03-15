package com.github.paganini2008.transport;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.converter.ConvertUtils;

/**
 * 
 * TupleImpl
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class TupleImpl extends TreeMap<String, Object> implements Tuple, Serializable {

	private static final long serialVersionUID = -7400874886336157587L;

	public TupleImpl() {
		super();
		setField("timestamp", System.currentTimeMillis());
	}

	public TupleImpl(Comparator<String> comparator) {
		super(comparator);
	}

	public TupleImpl(Map<String, ?> kwargs) {
		super(kwargs);
		setField("timestamp", System.currentTimeMillis());
	}

	@Override
	public boolean hasField(String fieldName) {
		return containsKey(fieldName);
	}

	@Override
	public void setField(String fieldName, Object value) {
		put(fieldName, value);
	}

	@Override
	public Object getField(String fieldName) {
		return get(fieldName);
	}

	@Override
	public <T> T getField(String fieldName, Class<T> requiredType) {
		return ConvertUtils.convertValue(getField(fieldName), requiredType);
	}

	@Override
	public void fill(Object object) {
		for (String key : keySet()) {
			BeanUtils.setProperty(object, key, get(key));
		}
	}

	@Override
	public Map<String, Object> toMap() {
		return Collections.unmodifiableMap(this);
	}

	@Override
	public Tuple copy() {
		return Tuple.wrap(this);
	}

}
