package com.github.paganini2008.springworld.transport.buffer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * RedisBufferZone
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class RedisBufferZone implements BufferZone {

	@Qualifier("bufferzone-redis-template")
	@Autowired
	private RedisTemplate<String, Object> template;

	@Autowired
	private InstanceId instanceId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.transport.bufferzone.shared:true}")
	private boolean shared;

	@Override
	public void set(String collectionName, Tuple tuple) {
		template.opsForList().leftPush(keyFor(collectionName), tuple);
	}

	@Override
	public List<Tuple> get(String collectionName, int pullSize) {
		List<Tuple> list = new ArrayList<Tuple>();
		Tuple tuple;
		int i = 0;
		while (null != (tuple = (Tuple) template.opsForList().leftPop(keyFor(collectionName))) && i++ < pullSize) {
			list.add(tuple);
		}
		return list;
	}

	@Override
	public int size(String collectionName) {
		return template.opsForList().size(keyFor(collectionName)).intValue();
	}

	protected String keyFor(String collectionName) {
		return "spring:application:transport:" + applicationName + ":bufferzone:" + collectionName + (shared ? "" : ":" + instanceId.get());
	}

}
