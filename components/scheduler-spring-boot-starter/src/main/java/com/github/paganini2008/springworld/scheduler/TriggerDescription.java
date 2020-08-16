package com.github.paganini2008.springworld.scheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;

/**
 * 
 * TriggerDetail
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@JsonInclude(value = Include.NON_NULL)
public class TriggerDescription {

	private String cron;
	private Long delay;
	private SchedulingUnit delaySchedulingUnit;
	private Long period;
	private SchedulingUnit periodSchedulingUnit;
	private SchedulingMode schedulingMode;
	private String dependencies;

	public void setCron(String cron) {
		this.cron = cron;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public void setDelaySchedulingUnit(int delaySchedulingUnit) {
		this.delaySchedulingUnit = SchedulingUnit.valueOf(delaySchedulingUnit);
	}

	public void setDelaySchedulingUnit(SchedulingUnit delaySchedulingUnit) {
		this.delaySchedulingUnit = delaySchedulingUnit;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public void setPeriodSchedulingUnit(SchedulingUnit periodSchedulingUnit) {
		this.periodSchedulingUnit = periodSchedulingUnit;
	}

	public void setPeriodSchedulingUnit(int periodSchedulingUnit) {
		this.periodSchedulingUnit = SchedulingUnit.valueOf(periodSchedulingUnit);
	}

	public void setSchedulingMode(int schedulingMode) {
		this.schedulingMode = SchedulingMode.valueOf(schedulingMode);
	}

	public void setSchedulingMode(SchedulingMode schedulingMode) {
		this.schedulingMode = schedulingMode;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static void main(String[] args) {
		String str = "{\"cron\":\"0 0 1 * * ?\"}";
		System.out.println(JacksonUtils.parseJson(str, TriggerDescription.class));
	}

}
