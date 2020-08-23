package com.github.paganini2008.springworld.myjob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * RunningState
 *
 * @author Fred Feng
 * @since 1.0
 */
public enum RunningState {

	FAILED(0), COMPLETED(1), SKIPPED(2), RUNNING(99);

	private final int value;

	private RunningState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@JsonValue
	public String getRepr() {
		return this.name().toLowerCase();
	}

	@JsonCreator
	public static RunningState valueOf(int value) {
		for (RunningState state : RunningState.values()) {
			if (state.getValue() == value) {
				return state;
			}
		}
		throw new IllegalArgumentException("Unknown value: " + value);
	}

}
