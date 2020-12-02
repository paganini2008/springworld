package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * AbstractConditionalCompletion
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public abstract class AbstractConditionalCompletion implements ConditionalCompletion {

	private final Map<Long, AtomicBoolean> completableMap = new ConcurrentHashMap<Long, AtomicBoolean>();

	@Override
	public void reset(Long catalogId) {
		completableMap.remove(catalogId);
	}

	protected void set(Long catalogId, boolean completed) {
		MapUtils.get(completableMap, catalogId, () -> {
			return new AtomicBoolean(false);
		}).set(completed);
	}

	@Override
	public boolean isCompleted(Tuple tuple) {
		final Long catalogId = (Long) tuple.getField("catalogId");
		return completableMap.containsKey(catalogId) ? completableMap.get(catalogId).get() : false;
	}

}
