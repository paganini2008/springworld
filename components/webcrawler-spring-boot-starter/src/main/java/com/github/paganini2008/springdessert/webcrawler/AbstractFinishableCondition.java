package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * AbstractFinishableCondition
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public abstract class AbstractFinishableCondition implements FinishableCondition {

	protected final Map<Long, AtomicBoolean> finishableMap = new ConcurrentHashMap<Long, AtomicBoolean>();

	@Override
	public void reset(Long catalogId) {
		finishableMap.remove(catalogId);
	}

	@Override
	public boolean isFinished(Tuple tuple) {
		final Long catalogId = (Long) tuple.getField("catalogId");
		return finishableMap.containsKey(catalogId) ? finishableMap.get(catalogId).get() : false;
	}

}
