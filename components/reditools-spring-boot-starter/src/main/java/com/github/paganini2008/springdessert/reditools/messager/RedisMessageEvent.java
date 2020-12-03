package com.github.paganini2008.springdessert.reditools.messager;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * RedisMessageEvent
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RedisMessageEvent extends ApplicationEvent {

	private static final long serialVersionUID = 5563838735572037403L;

	public RedisMessageEvent(RedisMessageEntity entity) {
		super(entity);
	}

	public String getChannel() {
		return getSource().getChannel();
	}

	public Object getMessage() {
		return getSource().getMessage();
	}

	@Override
	public RedisMessageEntity getSource() {
		return (RedisMessageEntity) super.getSource();
	}

}
