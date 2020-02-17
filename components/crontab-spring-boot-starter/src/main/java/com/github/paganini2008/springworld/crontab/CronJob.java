package com.github.paganini2008.springworld.crontab;

import com.github.paganini2008.devtools.scheduler.cron.CronExpression;

/**
 * 
 * CronJob
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface CronJob extends Job {

	CronExpression getCronExpression();

}
