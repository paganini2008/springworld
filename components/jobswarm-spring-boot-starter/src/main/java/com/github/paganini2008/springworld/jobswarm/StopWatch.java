package com.github.paganini2008.springworld.jobswarm;

import java.util.Date;

/**
 * 
 * StopWatch
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface StopWatch {

	JobState startJob(long traceId, JobKey jobKey, Date startDate);

	JobState finishJob(long traceId, JobKey jobKey, Date startDate, RunningState runningState, int retries);

}
