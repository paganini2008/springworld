package com.github.paganini2008.springworld.joblink;

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

	JobState startJob(long traceId, JobKey jobKey, Date startTime);

	JobState finishJob(long traceId, JobKey jobKey, Date startTime, RunningState runningState, int retries);

}
