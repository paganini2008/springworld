package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * CronJob
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface CronJob extends Job {

	String getCronExpression();

}
