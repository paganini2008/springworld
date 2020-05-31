package com.github.paganini2008.springworld.redisplus.messager;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * RedisMessageAckEvent
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RedisMessageAckEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6823499601966781170L;

	public RedisMessageAckEvent(RedisMessageEntity entity) {
		super(entity);
	}

	@Override
	public RedisMessageEntity getSource() {
		return (RedisMessageEntity) super.getSource();
	}
}
