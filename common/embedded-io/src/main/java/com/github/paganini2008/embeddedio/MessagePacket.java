package com.github.paganini2008.embeddedio;

import java.io.Serializable;
import java.util.List;

import com.github.paganini2008.devtools.collection.CollectionUtils;

/**
 * 
 * MessagePacket
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class MessagePacket implements Serializable {

	private static final long serialVersionUID = -4067748468303232269L;
	private final List<Object> messages;
	private long length;

	MessagePacket(List<Object> messages, long length) {
		this.messages = messages;
		this.length = length;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public List<Object> getMessages() {
		return messages;
	}

	public Object getMessage() {
		return CollectionUtils.isNotEmpty(messages) ? messages.get(0) : null;
	}

	public static MessagePacket of(List<Object> messages, long length) {
		return new MessagePacket(messages, length);
	}

}
