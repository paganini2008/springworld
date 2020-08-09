package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * PeriodicJob
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface PeriodicJob extends Job {

	default long getDelay() {
		return getPeriod();
	}

	default SchedulingUnit getDelaySchedulingUnit() {
		return getPeriodSchedulingUnit();
	}

	long getPeriod();

	default SchedulingUnit getPeriodSchedulingUnit() {
		return SchedulingUnit.SECONDS;
	}

	default SchedulingMode getSchedulingMode() {
		return SchedulingMode.FIXED_RATE;
	}

}
