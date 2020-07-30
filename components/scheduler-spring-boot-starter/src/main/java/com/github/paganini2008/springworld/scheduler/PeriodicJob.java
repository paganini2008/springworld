package com.github.paganini2008.springworld.scheduler;

import java.util.concurrent.TimeUnit;

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

	default TimeUnit getDelayTimeUnit() {
		return getPeriodTimeUnit();
	}

	long getPeriod();

	default TimeUnit getPeriodTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}

	default SchedulingMode getSchedulingMode() {
		return SchedulingMode.FIXED_RATE;
	}

}
