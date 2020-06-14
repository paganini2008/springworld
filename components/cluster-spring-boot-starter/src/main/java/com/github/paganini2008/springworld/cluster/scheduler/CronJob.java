package com.github.paganini2008.springworld.cluster.scheduler;

/**
 * 
 * CronJob
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface CronJob extends Job {

	String getCronExpression();

}
