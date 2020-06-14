package com.github.paganini2008.springworld.cluster.scheduler;

import java.util.concurrent.TimeUnit;

/**
 * 
 * ScheduledJob
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface ScheduledJob extends Job {

	default long getDelay() {
		return getPeriod();
	}

	default TimeUnit getDelayUnit() {
		return getPeriodUnit();
	}

	long getPeriod();

	default TimeUnit getPeriodUnit() {
		return TimeUnit.MILLISECONDS;
	}

	default RunningMode getRunningMode() {
		return RunningMode.FIXED_RATE;
	}

}
