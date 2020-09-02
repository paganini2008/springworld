package com.github.paganini2008.springworld.crontab;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * JobAction
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum JobAction {

	CREATION(0), DELETION(1), REFRESH(2);

	private final int value;

	private JobAction(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@JsonCreator
	public static JobAction valueOf(int value) {
		for (JobAction jobAction : JobAction.values()) {
			if (jobAction.getValue() == value) {
				return jobAction;
			}
		}
		throw new IllegalArgumentException("Unknown jobAction: " + value);
	}

}
