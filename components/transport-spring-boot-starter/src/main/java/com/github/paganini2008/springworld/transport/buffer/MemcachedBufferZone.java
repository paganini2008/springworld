package com.github.paganini2008.springworld.transport.buffer;

import java.net.InetSocketAddress;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.xmemcached.MemcachedTemplate;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.TupleImpl;

/**
 * 
 * MemcachedBufferZone
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class MemcachedBufferZone implements BufferZone {

	private static final int DEFAULT_EXPIRATION = 60;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Value("${spring.transport.bufferzone.cooperative:true}")
	private boolean cooperative;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private MemcachedTemplate memcachedOperations;

	@Override
	public void set(String collectionName, Tuple tuple) throws Exception {
		memcachedOperations.push(keyFor(collectionName), DEFAULT_EXPIRATION, tuple);
	}

	@Override
	public Tuple get(String collectionName) throws Exception {
		return memcachedOperations.pop(keyFor(collectionName), TupleImpl.class);
	}

	private String keyFor(String collectionName) {
		return "transport:bufferzone:" + collectionName + ":" + applicationName + (cooperative ? "" : ":" + clusterId.get());
	}

	@Override
	public int size(String collectionName) throws Exception {
		Map<InetSocketAddress, Map<String, String>> result = memcachedOperations.getClient().getStats();
		int total = 0;
		if (result != null) {
			for (Map<String, String> map : result.values()) {
				total += map.containsKey("curr_items") ? Integer.parseInt(map.get("curr_items")) : 0;
			}
		}
		return total;
	}

}
