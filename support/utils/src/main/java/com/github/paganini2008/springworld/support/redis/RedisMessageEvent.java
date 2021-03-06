package com.github.paganini2008.springworld.support.redis;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * RedisMessageEvent
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public class RedisMessageEvent extends ApplicationEvent {

	private static final long serialVersionUID = -440558170671797251L;

	public RedisMessageEvent(String channel, Object message) {
		super(message);
		this.channel = channel;
	}

	private final String channel;

	public String getChannel() {
		return channel;
	}

	public Object getMessage() {
		return super.getSource();
	}

}
