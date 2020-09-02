package com.github.paganini2008.springworld.crontab;

/**
 * 
 * ScheduleAdmin
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ScheduleAdmin {

	JobState scheduleJob(JobKey jobKey);
	
	JobState unscheduleJob(JobKey jobKey);
	
}
