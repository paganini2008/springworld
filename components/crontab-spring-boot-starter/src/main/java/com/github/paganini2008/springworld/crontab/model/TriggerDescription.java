package com.github.paganini2008.springworld.crontab.model;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.SchedulingUnit;
import com.github.paganini2008.springworld.crontab.TriggerType;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * TriggerDetail
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class TriggerDescription {

	private Cron cron;
	private Periodic periodic;
	private Serial serial;

	public TriggerDescription() {
	}

	public TriggerDescription(TriggerType triggerType) {
		switch (triggerType) {
		case CRON:
			cron = new Cron();
			break;
		case PERIODIC:
			periodic = new Periodic();
			break;
		case SERIAL:
			serial = new Serial();
			break;
		}
	}

	@Data
	public static class Cron {

		private String expression;

		public Cron() {
		}

		public Cron(String expression) {
			this.expression = expression;
		}
	}

	@Data
	public static class Periodic {

		private long period;
		private SchedulingUnit schedulingUnit;
		private boolean fixedRate;

		public Periodic() {
		}

		public Periodic(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
			this.period = period;
			this.schedulingUnit = schedulingUnit;
			this.fixedRate = fixedRate;
		}

	}

	public static class Serial {

		private Set<JobKey> dependencies = new TreeSet<JobKey>();

		public Serial() {
		}

		public void setDependencies(JobKey[] jobKeys) {
			if (ArrayUtils.isNotEmpty(jobKeys)) {
				dependencies.addAll(Arrays.asList(jobKeys));
			}
		}

		public JobKey[] getDependencies() {
			return dependencies.toArray(new JobKey[0]);
		}

	}

}
