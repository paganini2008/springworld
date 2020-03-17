package com.github.paganini2008.springworld.transport.buffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.paganini2008.devtools.collection.LruQueue;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * MemoryBufferZone
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class MemoryBufferZone implements BufferZone {

	private final ConcurrentMap<String, LruQueue<Tuple>> cache = new ConcurrentHashMap<String, LruQueue<Tuple>>();

	private final int maxSize;

	public MemoryBufferZone() {
		this(1024);
	}

	public MemoryBufferZone(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public void set(String collectionName, Tuple tuple) {
		LruQueue<Tuple> q = cache.get(collectionName);
		if (q == null) {
			cache.putIfAbsent(collectionName, new LruQueue<Tuple>(maxSize));
			q = cache.get(collectionName);
		}
		q.offer(tuple);
	}

	@Override
	public Tuple get(String collectionName) {
		LruQueue<Tuple> q = cache.get(collectionName);
		return q != null ? q.poll() : null;
	}

	@Override
	public int size(String collectionName) {
		LruQueue<Tuple> q = cache.get(collectionName);
		return q != null ? q.size() : 0;
	}

}
