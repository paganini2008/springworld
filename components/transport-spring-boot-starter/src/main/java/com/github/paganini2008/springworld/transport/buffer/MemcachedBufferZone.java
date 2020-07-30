package com.github.paganini2008.springworld.transport.buffer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.InstanceId;
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

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Value("${spring.application.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Value("${spring.application.transport.bufferzone.shared:true}")
	private boolean shared;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private MemcachedTemplate memcachedOperations;

	@Override
	public void set(String collectionName, Tuple tuple) throws Exception {
		memcachedOperations.push(keyFor(collectionName), DEFAULT_EXPIRATION, tuple);
	}

	@Override
	public List<Tuple> get(String collectionName, int pullSize) throws Exception {
		List<Tuple> list = new ArrayList<Tuple>();
		Tuple tuple;
		int i = 0;
		while (null != (tuple = memcachedOperations.pop(keyFor(collectionName), TupleImpl.class)) && i++ < pullSize) {
			list.add(tuple);
		}
		return list;
	}

	protected String keyFor(String collectionName) {
		return "spring:application:transport:" + clusterName + ":bufferzone:" + collectionName + (shared ? "" : ":" + instanceId.get());
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
