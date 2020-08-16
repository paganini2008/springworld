package com.github.paganini2008.springworld.scheduler;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * SchedulingUnit
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum SchedulingUnit {

	SECONDS(0, TimeUnit.SECONDS), MINUTES(0, TimeUnit.MINUTES), HOURS(0, TimeUnit.HOURS), DAYS(0, TimeUnit.DAYS);

	private final int value;
	private final TimeUnit timeUnit;

	private SchedulingUnit(int value, TimeUnit timeUnit) {
		this.value = value;
		this.timeUnit = timeUnit;

	}

	@JsonValue
	public int getValue() {
		return value;
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