package com.github.paganini2008.springworld.scheduler;

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

	NOT_SCHEDULED(1), SCHEDULING(2), RUNNING(3), PAUSED(4), FINISHED(5);

	private JobState(int value) {
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}
	
	@JsonValue
	public String getRepr() {
		return this.name();
	}

	@JsonCreator
	public static JobState valueOf(int value) {
		for (JobState jobState : JobState.values()) {
			if (jobState.getValue() == value) {
				return jobState;
			}
		}
		throw new IllegalArgumentException("Unknown value of JobState: " + value);
	}

}
