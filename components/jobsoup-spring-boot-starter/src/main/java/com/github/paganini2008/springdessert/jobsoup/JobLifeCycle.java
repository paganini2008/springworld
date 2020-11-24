package com.github.paganini2008.springdessert.jobsoup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * JobLifeCycle
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum JobLifeCycle implements EnumConstant {

	CREATION(0, "creation"), DELETION(1, "deletion"), REFRESH(2, "refresh");

	private final int value;
	private final String repr;

	private JobLifeCycle(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@Override
	public String getRepr() {
		return repr;
	}

	@JsonCreator
	public static JobLifeCycle valueOf(int value) {
		for (JobLifeCycle jobAction : JobLifeCycle.values()) {
			if (jobAction.getValue() == value) {
				return jobAction;
			}
		}
		throw new IllegalArgumentException("Unknown jobAction: " + value);
	}

}
