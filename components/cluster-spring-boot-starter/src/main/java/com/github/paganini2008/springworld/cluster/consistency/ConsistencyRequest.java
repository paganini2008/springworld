package com.github.paganini2008.springworld.cluster.consistency;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.date.DateUtils;

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

	public static final String PREPARATION_OPERATION_REQUEST = "<Perparation Operation Request>";
	public static final String PREPARATION_OPERATION_RESPONSE = "<Perparation Operation Response>";
	public static final String COMMITMENT_OPERATION_REQUEST = "<Commitment Operation Request>";
	public static final String COMMITMENT_OPERATION_RESPONSE = "<Commitment Operation Response>";
	public static final String LEARNING_OPERATION_REQUEST = "<Learning Operation Request>";
	public static final String LEARNING_OPERATION_RESPONSE = "<Learning Operation Response>";
	public static final String TIMEOUT_OPERATION_REQUEST = "<Timeout Operation Request>";
	public static final String TIMEOUT_OPERATION_RESPONSE = "<Timeout Operation Response>";

	private String id;
	private String instanceId;
	private String name;
	private Object value;
	private long serial;
	private long round;
	private long timestamp;
	private int timeout;

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

	public ConsistencyRequest setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public boolean hasExpired() {
		return timeout > 0 && System.currentTimeMillis() - timestamp > timeout * 1000;
	}

	public ConsistencyResponse ack(String instanceId, boolean acceptable) {
		return new ConsistencyResponse(this, instanceId, acceptable);
	}

	public ConsistencyRequest copy() {
		return ConsistencyRequest.of(instanceId).setName(name).setRound(round).setValue(value).setSerial(serial);
	}

	public String toString() {
		return "[" + round + "/" + serial + "] name: " + name + ", value: " + (value != null ? value.getClass() : "NULL") + ", date: "
				+ DateUtils.format(timestamp);
	}

}
