package com.github.paganini2008.springworld.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * TriggerType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum TriggerType {

	CRON(0), PERIODIC(1);

	private final int value;

	private TriggerType(int value) {
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
	public static TriggerType valueOf(int value) {
		for (TriggerType jobType : TriggerType.values()) {
			if (jobType.getValue() == value) {
				return jobType;
			}
		}
		throw new IllegalArgumentException("Unknown job type: " + value);
	}

	public static TriggerType valueOf(Job job) {
		if (job instanceof CronJob) {
			return TriggerType.CRON;
		} else if (job instanceof PeriodicJob) {
			return TriggerType.PERIODIC;
		}
		throw new IllegalArgumentException("Unknown job class: " + job.getClass());
	}

}
