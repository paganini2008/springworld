package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;

import com.github.paganini2008.devtools.StringUtils;
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
public class TriggerDescription implements Serializable {

	private static final long serialVersionUID = -6796406296041227349L;
	private String cron;
	private long delay;
	private SchedulingUnit delaySchedulingUnit;
	private long period;
	private SchedulingUnit periodSchedulingUnit;
	private SchedulingMode schedulingMode;
	private String[] dependencies;

	public void setCron(String cron) {
		this.cron = cron;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void setDelaySchedulingUnit(int delaySchedulingUnit) {
		this.delaySchedulingUnit = SchedulingUnit.valueOf(delaySchedulingUnit);
	}

	public void setDelaySchedulingUnit(SchedulingUnit delaySchedulingUnit) {
		this.delaySchedulingUnit = delaySchedulingUnit;
	}

	public void setPeriod(long period) {
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
		this.dependencies = StringUtils.isNotBlank(dependencies) ? dependencies.split(",") : new String[0];
	}

	public void setDependencies(String[] dependencies) {
		this.dependencies = dependencies;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
