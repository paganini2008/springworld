package com.github.paganini2008.springworld.redisplus.messager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisMessageAckEventListener
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RedisMessageAckEventListener implements ApplicationListener<RedisMessageAckEvent> {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onApplicationEvent(RedisMessageAckEvent event) {
		if (log.isTraceEnabled()) {
			log.trace("Ack: " + event.getSource());
		}
		RedisMessageEntity messageEntity = event.getSource();
		if (!messageEntity.isOk() && retry(messageEntity)) {
			if (messageEntity.getDelay() != -1 && messageEntity.getTimeUnit() != null) {
				redisMessageSender.sendMessage(messageEntity.getChannel(), messageEntity.getMessage());
			} else {
				redisMessageSender.sendEphemeralMessage(messageEntity.getChannel(), messageEntity.getMessage(), messageEntity.getDelay(),
						messageEntity.getTimeUnit());
			}
		}
	}

	protected boolean retry(RedisMessageEntity messageEntity) {
		return true;
	}

}
