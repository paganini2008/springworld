package com.github.paganini2008.springworld.jobclick;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * JobAction
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum JobAction implements EnumConstant {

	CREATION(0, "creation"), DELETION(1, "deletion"), REFRESH(2, "refresh");

	private final int value;
	private final String repr;

	private JobAction(int value, String repr) {
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
	public static JobAction valueOf(int value) {
		for (JobAction jobAction : JobAction.values()) {
			if (jobAction.getValue() == value) {
				return jobAction;
			}
		}
		throw new IllegalArgumentException("Unknown jobAction: " + value);
	}

}
