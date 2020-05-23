package com.github.paganini2008.springworld.cluster.consistency;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;

/**
 * 
 * ConsistencyRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Getter
public class ConsistencyRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PREPARATION_REQUEST = "<Perparation Request>";
	public static final String PREPARATION_RESPONSE = "<Perparation Response>";
	public static final String COMMITMENT_REQUEST = "<Commitment Request>";
	public static final String COMMITMENT_RESPONSE = "<Commitment Response>";
	public static final String LEARNING_REQUEST = "<Learning Request>";
	public static final String LEARNING_RESPONSE = "<Learning Response>";

	private String id;
	private String instanceId;
	private String name;
	private Object value;
	private long serial;
	private long round;
	private long timestamp;

	public ConsistencyRequest() {
	}

	ConsistencyRequest(String instanceId) {
		this.id = UUID.randomUUID().toString();
		this.instanceId = instanceId;
		this.timestamp = System.currentTimeMillis();
	}

	public static ConsistencyRequest of(String instanceId) {
		return new ConsistencyRequest(instanceId);
	}

	public ConsistencyRequest setName(String name) {
		this.name = name;
		return this;
	}

	public ConsistencyRequest setValue(Object value) {
		this.value = value;
		return this;
	}

	public ConsistencyRequest setRound(long round) {
		this.round = round;
		return this;
	}

	public ConsistencyRequest setSerial(long serial) {
		this.serial = serial;
		return this;
	}

	public ConsistencyResponse ack(String instanceId, boolean acceptable) {
		return new ConsistencyResponse(this, instanceId, acceptable);
	}

	public ConsistencyRequest copy() {
		return ConsistencyRequest.of(instanceId).setName(name).setRound(round).setValue(value).setSerial(serial);
	}

}
