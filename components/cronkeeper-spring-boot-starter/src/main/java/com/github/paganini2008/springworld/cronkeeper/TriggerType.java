package com.github.paganini2008.springworld.cronkeeper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * TriggerType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum TriggerType implements EnumConstant {

	CRON(1, "Cron"),

	PERIODIC(2, "Periodic"),

	SERIAL(3, "Serial"),

	COMBINED(4, "Combined");

	private final int value;
	private final String repr;

	private TriggerType(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	public String getRepr() {
		return repr;
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
