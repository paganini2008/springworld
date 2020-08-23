package com.github.paganini2008.springworld.myjob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * TriggerType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum TriggerType {

	CRON(1),

	PERIODIC(2),

	SERIAL(3);

	private final int value;

	private TriggerType(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@JsonCreator
	public static TriggerType valueOf(int value) {
		for (TriggerType jobType : TriggerType.values()) {
			if (jobType.getValue() == value) {
				return jobType;
			}
		}
		throw new IllegalArgumentException("Unknown job type: " + value);
	}

}