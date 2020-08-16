package com.github.paganini2008.springworld.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * TriggerType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum TriggerType {

	CRON(1) {

		@Override
		public Trigger getTrigger(TriggerDescription triggerDescription) {
			return new CronTrigger(triggerDescription.getCron());
		}

	},

	PERIODIC(2) {
		@Override
		public Trigger getTrigger(TriggerDescription triggerDescription) {
			return new PeriodicTrigger(triggerDescription.getSchedulingMode(), triggerDescription.getDelay(),
					triggerDescription.getDelaySchedulingUnit(), triggerDescription.getPeriod(),
					triggerDescription.getPeriodSchedulingUnit());
		}
	},

	SERIAL(3) {
		@Override
		public Trigger getTrigger(TriggerDescription triggerDescription) {
			return new SerialTrigger(
					StringUtils.isNotBlank(triggerDescription.getDependencies()) ? triggerDescription.getDependencies().split(",")
							: new String[0]);
		}
	};

	private final int value;

	private TriggerType(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	public abstract Trigger getTrigger(TriggerDescription triggerDescription);

	@JsonCreator
	public static TriggerType valueOf(int value) {
		for (TriggerType jobType : TriggerType.values()) {
			if (jobType.getValue() == value) {
				return jobType;
			}
		}
		throw new IllegalArgumentException("Unknown job type: " + value);
	}

}