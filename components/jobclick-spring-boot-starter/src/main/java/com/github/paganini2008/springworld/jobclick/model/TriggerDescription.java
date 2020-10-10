package com.github.paganini2008.springworld.jobclick.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.SchedulingUnit;
import com.github.paganini2008.springworld.jobclick.TriggerType;

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

	private Cron cron;
	private Periodic periodic;
	private Serial serial;
	private Team team;

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
		case TEAM_CRON:
		case TEAM_PERIODIC:
		case TEAM_SERIAL:
			team = new Team();
			break;
		default:
			break;
		}
	}

	@Getter
	@Setter
	public static class Cron {

		private String expression;
		private Integer repeat;

		public Cron() {
		}

		public Cron(String expression) {
			this.expression = expression;
		}

		public String toString() {
			StringBuilder str = new StringBuilder(expression);
			if (repeat != null && repeat > 0) {
				str.append(" repeat ").append(repeat).append(" times");
			}
			return str.toString();
		}
	}

	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Periodic {

		private long period;
		private SchedulingUnit schedulingUnit;
		private boolean fixedRate;
		private Integer repeat;

		public Periodic() {
		}

		public Periodic(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
			this.period = period;
			this.schedulingUnit = schedulingUnit;
			this.fixedRate = fixedRate;
		}

		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(period).append(" ").append(schedulingUnit.getRepr());
			str.append(" fixedRate: ").append(fixedRate);
			if (repeat != null && repeat > 0) {
				str.append(" repeat ").append(repeat).append(" times");
			}
			return str.toString();
		}

	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Serial {

		private Set<JobKey> dependencies = new TreeSet<JobKey>();
		private Integer repeat;

		public Serial() {
		}

		public Serial setDependencies(JobKey[] dependencies) {
			if (ArrayUtils.isNotEmpty(dependencies)) {
				this.dependencies.addAll(Arrays.asList(dependencies));
			}
			return this;
		}

		public JobKey[] getDependencies() {
			return dependencies.toArray(new JobKey[0]);
		}

		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("Depend on Job: ");
			str.append(CollectionUtils.join(dependencies));
			if (repeat != null && repeat > 0) {
				str.append(", Repeat ").append(repeat).append(" times");
			}
			return str.toString();
		}

	}

	@JsonInclude(value = Include.NON_NULL)
	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Team {

		private Set<JobPeer> jobPeers = new TreeSet<JobPeer>();
		private Cron cron;
		private Periodic periodic;
		private Serial serial;
		private Float goal;

		public Team() {
		}

		public Team setTriggerType(TriggerType triggerType) {
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
			default:
				throw new IllegalStateException("For trigger type: " + triggerType);
			}
			return this;
		}

		public Team setCronExpression(String cronExpression) {
			getCron().setExpression(cronExpression);
			return this;
		}

		public Team setPeriod(long period) {
			getPeriodic().setPeriod(period);
			return this;
		}

		public Team setFixedRate(boolean fixedRate) {
			getPeriodic().setFixedRate(fixedRate);
			return this;
		}

		public Team setSchedulingUnit(SchedulingUnit schedulingUnit) {
			getPeriodic().setSchedulingUnit(schedulingUnit);
			return this;
		}

		public Team setDependencies(JobKey[] dependencies) {
			getSerial().setDependencies(dependencies);
			return this;
		}

		public Team add(JobPeer jobPeer) {
			jobPeers.add(jobPeer);
			return this;
		}

		public JobPeer[] getJobPeers() {
			return jobPeers.toArray(new JobPeer[0]);
		}

	}

}
