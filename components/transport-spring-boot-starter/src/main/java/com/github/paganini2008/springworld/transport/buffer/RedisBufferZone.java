package com.github.paganini2008.springworld.transport.buffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.cluster.ClusterId;
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
	private ClusterId clusterId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.transport.bufferzone.cooperative:true}")
	private boolean cooperative;

	@Override
	public void set(String name, Tuple tuple) {
		template.opsForList().leftPush(keyFor(name), tuple);
	}

	@Override
	public Tuple get(String name) {
		return (Tuple) template.opsForList().leftPop(keyFor(name));
	}

	@Override
	public int size(String name) {
		return template.opsForList().size(keyFor(name)).intValue();
	}

	private String keyFor(String name) {
		return "transport:bufferzone:" + name + ":" + applicationName + (cooperative ? "" : ":" + clusterId.get());
	}

}
