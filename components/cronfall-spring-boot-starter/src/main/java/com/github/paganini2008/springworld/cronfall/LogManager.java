package com.github.paganini2008.springworld.cronfall;

import com.github.paganini2008.devtools.ExceptionUtils;

/**
 * 
 * LogManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LogManager {

	default void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, Throwable e) {
		log(traceId, jobKey, logLevel, messagePattern, args, ExceptionUtils.toArray(e));
	}

	void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] stackTraces);

	default void error(long traceId, JobKey jobKey, Throwable e) {
		error(traceId, jobKey, ExceptionUtils.toArray(e));
	}

	void error(long traceId, JobKey jobKey, String[] stackTraces);

}
