package com.github.paganini2008.springworld.redisplus.messager;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.devtools.ObjectUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * RedisMessageEntity
 *
 * @author Fred Feng
 * @version 1.0
 */
@Getter
@Setter
public class RedisMessageEntity implements Serializable {

	private static final long serialVersionUID = -8864323748615322076L;

	public static final RedisMessageEntity EMPTY = RedisMessageEntity.of("", null);

	private String id;
	private long timestamp;
	private String channel;
	private Object message;
	private long delay = -1;
	private TimeUnit timeUnit;
	private boolean ok;

	public RedisMessageEntity() {
	}

	RedisMessageEntity(String channel, Object message) {
		this.id = UUID.randomUUID().toString();
		this.channel = channel;
		this.message = message;
		this.timestamp = System.currentTimeMillis();
	}

	public static RedisMessageEntity of(String channel, Object message) {
		return new RedisMessageEntity(channel, message);
	}

	public String toString() {
		return "[RedisMessageEntity] channel: " + channel + ", message: " + ObjectUtils.toStringSelectively(message);
	}

}
