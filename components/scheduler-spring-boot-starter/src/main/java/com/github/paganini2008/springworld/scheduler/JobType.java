package com.github.paganini2008.springworld.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 * JobType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum JobType {

	CRON(0), PERIODIC(1), SERIALIZABLE(2);

	private final int value;

	private JobType(int value) {
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
	public static JobType valueOf(int value) {
		for (JobType jobType : JobType.values()) {
			if (jobType.getValue() == value) {
				return jobType;
			}
		}
		throw new IllegalArgumentException("Unknown job type: " + value);
	}

	public static JobType valueOf(Job job) {
		if (job instanceof CronJob) {
			return JobType.CRON;
		} else if (job instanceof PeriodicJob) {
			return JobType.PERIODIC;
		} else if (job instanceof SerializableJob) {
			return JobType.SERIALIZABLE;
		}
		throw new IllegalArgumentException("Unknown job class: " + job.getClass());
	}

}
