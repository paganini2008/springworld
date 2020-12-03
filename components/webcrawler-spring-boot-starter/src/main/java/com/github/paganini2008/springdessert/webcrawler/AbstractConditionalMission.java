package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * AbstractConditionalMission
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public abstract class AbstractConditionalMission implements ConditionalMission {

	private final Map<Long, AtomicBoolean> completableMap = new ConcurrentHashMap<Long, AtomicBoolean>();

	@Override
	public void reset(long catalogId) {
		completableMap.remove(catalogId);
	}

	protected void set(long catalogId, boolean completed) {
		MapUtils.get(completableMap, catalogId, () -> {
			return new AtomicBoolean(false);
		}).set(completed);
	}

	@Override
	public boolean isCompleted(long catalogId, Tuple tuple) {
		return completableMap.containsKey(catalogId) ? completableMap.get(catalogId).get() : false;
	}

}
