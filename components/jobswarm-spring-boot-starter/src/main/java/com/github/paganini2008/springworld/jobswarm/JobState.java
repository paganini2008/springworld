package com.github.paganini2008.springworld.jobswarm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * JobState
 *
 * @author Fred Feng
 * @since 1.0
 */
public enum JobState implements EnumConstant {

	NOT_SCHEDULED(0, "Not scheduled"), SCHEDULING(1, "Scheduling"), RUNNING(2, "Running"), PAUSED(3, "Paused"), FINISHED(4,
			"Finished"), NONE(99, "None");

	private JobState(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	private final int value;
	private final String repr;

	@JsonValue
	public int getValue() {
		return value;
	}

	@Override
	public String getRepr() {
		return repr;
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
