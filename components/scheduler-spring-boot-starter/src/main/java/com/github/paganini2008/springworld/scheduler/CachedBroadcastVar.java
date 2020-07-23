package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cached.base.Cache;

/**
 * 
 * CachedBroadcastVar
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class CachedBroadcastVar implements BroadcastVar {

	@Autowired
	private Cache cache;

	@Override
	public Object readVar(String name) {
		return cache.get(name);
	}

	@Override
	public void writeVar(String name, Object value) {
		cache.set(name, value);
	}

	@Override
	public boolean hasVar(String name) {
		return cache.hasKey(name);
	}

	@Override
	public int length() {
		return cache.size();
	}

}
