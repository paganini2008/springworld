package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * ConsistencyRequestSerialCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestSerialCache {

	private final ConcurrentMap<String, Map<Long, Long>> serials = new ConcurrentHashMap<String, Map<Long, Long>>();

	public void setSerial(String name, long round, long serial) {
		serials.get(name).put(round, serial);
	}

	public long getSerial(String name, long round) {
		Map<Long, Long> data = MapUtils.get(serials, name, () -> {
			return new ConcurrentHashMap<Long, Long>();
		});
		return data.getOrDefault(round, 0L);
	}

	public void clean(String name) {
		serials.remove(name);
	}

}
