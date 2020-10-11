package com.github.paganini2008.springworld.jobclick.model;

import java.io.Serializable;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.SchedulingUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class TriggerDescription implements Serializable {

	private static final long serialVersionUID = 7719080769264307755L;

	private @Nullable Cron cron;
	private @Nullable Periodic periodic;
	private @Nullable Serial serial;
	private @Nullable Milestone milestone;

	public TriggerDescription() {
	}

	public TriggerDescription(String cronExpression) {
		this.cron = new Cron(cronExpression);
	}

	public TriggerDescription(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
		this.periodic = new Periodic(period, schedulingUnit, fixedRate);
	}

	public TriggerDescription(JobKey... dependencies) {
		this.serial = new Serial(dependencies);
	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Serial implements Serializable {

		private static final long serialVersionUID = -8486773222061112232L;
		private JobKey[] dependencies;

		public Serial(JobKey... dependencies) {
			this.dependencies = dependencies;
		}

		public Serial setDependencies(JobKey[] dependencies) {
			this.dependencies = dependencies;
			return this;
		}

		public JobKey[] getDependencies() {
			return dependencies;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Cron implements Serializable {

		private static final long serialVersionUID = -1789487585777178180L;
		private String expression;

		public Cron() {
		}

		public Cron(String expression) {
			this.expression = expression;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Periodic implements Serializable {

		private static final long serialVersionUID = 2274953049040184466L;
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

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Milestone {

		private JobPeer[] cooperators;
		private Float goal;

		public Milestone() {
		}

		public Milestone(JobPeer[] cooperators, Float goal) {
			this.cooperators = cooperators;
			this.goal = goal;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

}
