package com.github.paganini2008.springworld.jobswarm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Cron;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Dependency;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Periodic;

/**
 * 
 * TriggerType
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum TriggerType implements EnumConstant {

	NONE(0, "None") {

		@Override
		public TriggerDescription getTriggerDescription() {
			return new TriggerDescription();
		}
	},

	CRON(1, "Cron") {

		@Override
		public TriggerDescription getTriggerDescription() {
			TriggerDescription triggerDescription = new TriggerDescription();
			triggerDescription.setCron(new Cron());
			return triggerDescription;
		}
	},

	PERIODIC(2, "Periodic") {

		@Override
		public TriggerDescription getTriggerDescription() {
			TriggerDescription triggerDescription = new TriggerDescription();
			triggerDescription.setPeriodic(new Periodic());
			return triggerDescription;
		}
	},

	DEPENDENT(3, "Dependent") {
		@Override
		public TriggerDescription getTriggerDescription() {
			TriggerDescription triggerDescription = new TriggerDescription();
			triggerDescription.setDependency(new Dependency());
			return triggerDescription;
		}
	};

	private final int value;
	private final String repr;

	private TriggerType(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	public String getRepr() {
		return repr;
	}

	public abstract TriggerDescription getTriggerDescription();

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
