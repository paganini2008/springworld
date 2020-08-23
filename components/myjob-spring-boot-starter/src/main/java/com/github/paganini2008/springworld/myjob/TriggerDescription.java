package com.github.paganini2008.springworld.myjob;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

	@Data
	public static class Serial {
		
		private String[] dependencies;

		public Serial(String[] dependencies) {
			this.dependencies = dependencies;
		}

		public Serial() {
		}

	}

	public static void main(String[] args) {
		String str = "{\"serial\":{\"dependencies\":[\"tester.healthCheckJob@com.allyes.springboot.tester.job.HealthCheckJob\"]}}";
		System.out.println(JacksonUtils.parseJson(str, TriggerDescription.class));
	}

}
