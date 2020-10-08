package com.github.paganini2008.springworld.joblink;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * SchedulingUnit
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum SchedulingUnit implements EnumConstant {

	SECONDS(0, "Per second", TimeUnit.SECONDS), MINUTES(1, "Per minute", TimeUnit.MINUTES), HOURS(2, "Per hour", TimeUnit.HOURS), DAYS(3,
			"Per day", TimeUnit.DAYS);

	private final int value;
	private final String repr;
	private final TimeUnit timeUnit;

	private SchedulingUnit(int value, String repr, TimeUnit timeUnit) {
		this.value = value;
		this.timeUnit = timeUnit;
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

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	@JsonCreator
	public static SchedulingUnit valueOf(int value) {
		for (SchedulingUnit unit : SchedulingUnit.values()) {
			if (unit.getValue() == value) {
				return unit;
			}
		}
		throw new IllegalArgumentException("Unknown SchedulingUnit: " + value);
	}

}
