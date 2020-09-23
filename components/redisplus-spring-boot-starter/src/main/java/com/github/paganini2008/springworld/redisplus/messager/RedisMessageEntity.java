package com.github.paganini2008.springworld.redisplus.messager;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.Assert;
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

	public static final RedisMessageEntity EMPTY = RedisMessageEntity.of("", null, false);

	private String id;
	private String channel;
	private Object message;
	private boolean ack;
	private long timestamp;

	public RedisMessageEntity() {
	}

	protected RedisMessageEntity(String channel, Object message, boolean ack) {
		Assert.hasNoText(channel, "Channel must be required for redis message");
		this.id = UUID.randomUUID().toString();
		this.channel = channel;
		this.message = message;
		this.ack = ack;
		this.timestamp = System.currentTimeMillis();
	}

	public static RedisMessageEntity of(String channel, Object message, boolean ack) {
		return new RedisMessageEntity(channel, message, ack);
	}

	public String toString() {
		return "[RedisMessageEntity] channel: " + channel + ", message: " + ObjectUtils.toStringSelectively(message);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + prime * id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RedisMessageEntity) {
			if (obj == this) {
				return true;
			}
			RedisMessageEntity messageEntity = (RedisMessageEntity) obj;
			return messageEntity.getId().equals(getId());
		}
		return false;
	}

}
