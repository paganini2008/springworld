package com.github.paganini2008.springworld.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * SchedulingMode
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum SchedulingMode {

	FIXED_RATE(1), FIXED_DELAY(2);

	private final int value;

	private SchedulingMode(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@JsonCreator
	public static SchedulingMode valueOf(int value) {
		for (SchedulingMode schedulingMode : SchedulingMode.values()) {
			if (schedulingMode.getValue() == value) {
				return schedulingMode;
			}
		}
		throw new IllegalArgumentException("Unknown jobState: " + value);
	}

}
