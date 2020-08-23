package com.github.paganini2008.springworld.myjob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * JobState
 *
 * @author Fred Feng
 * @since 1.0
 */
public enum JobState {

	NOT_SCHEDULED(0), SCHEDULING(1), RUNNING(2), PAUSED(3), FINISHED(4), NONE(99);

	private JobState(int value) {
		this.value = value;
	}

	private final int value;

	@JsonValue
	public int getValue() {
		return value;
	}

	@JsonCreator
	public static JobState valueOf(int value) {
		for (JobState jobState : JobState.values()) {
			if (jobState.getValue() == value) {
				return jobState;
			}
		}
		throw new IllegalArgumentException("Unknown jobState: " + value);
	}

}