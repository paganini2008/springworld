package com.github.paganini2008.springworld.jobclick;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * DependencyType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum DependencyType implements EnumConstant {

	NONE(0, ""), PARALLEL(1, "Parallel"), SERIAL(2, "Serial");

	private final int value;
	private final String repr;

	private DependencyType(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	@Override
	@JsonValue
	public int getValue() {
		return value;
	}

	@Override
	public String getRepr() {
		return repr;
	}

	@JsonCreator
	public static DependencyType valueOf(int value) {
		for (DependencyType type : DependencyType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown DependencyType: " + value);
	}

}
