package com.github.paganini2008.springworld.crontab;

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

	JobState startJob(JobKey jobKey, Date startTime);

	JobState finishJob(JobKey jobKey, Date startTime, RunningState runningState, String[] errorStackTracks);

}
