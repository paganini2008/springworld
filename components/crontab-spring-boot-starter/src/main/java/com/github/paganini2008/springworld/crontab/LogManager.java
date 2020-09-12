package com.github.paganini2008.springworld.crontab;

/**
 * 
 * LogManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LogManager {

	void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] errorStackTracks);

	void error(long traceId, JobKey jobKey, String[] errorStackTracks);

}
