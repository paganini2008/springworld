package com.github.paganini2008.springworld.cronfall;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.redisplus.common.RedisUUID;

/**
 * 
 * UuidTraceIdGenerator
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UuidTraceIdGenerator implements TraceIdGenerator {

	@Autowired
	private RedisUUID redisUUID;

	@Override
	public long generateTraceId(JobKey jobKey) {
		return redisUUID.createUUID().timestamp();
	}

}
