package com.github.paganini2008.springworld.transport.buffer;

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
	private InstanceId clusterId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.transport.bufferzone.cooperative:true}")
	private boolean cooperative;

	@Override
	public void set(String collectionName, Tuple tuple) {
		template.opsForList().leftPush(keyFor(collectionName), tuple);
	}

	@Override
	public Tuple get(String collectionName) {
		return (Tuple) template.opsForList().leftPop(keyFor(collectionName));
	}

	@Override
	public int size(String collectionName) {
		return template.opsForList().size(keyFor(collectionName)).intValue();
	}

	private String keyFor(String collectionName) {
		return "transport:bufferzone:" + collectionName + ":" + applicationName + (cooperative ? "" : ":" + clusterId.get());
	}

}
