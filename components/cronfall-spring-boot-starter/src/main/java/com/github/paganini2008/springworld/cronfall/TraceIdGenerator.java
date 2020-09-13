package com.github.paganini2008.springworld.cronfall;

/**
 * 
 * TraceIdGenerator
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface TraceIdGenerator {

	long generateTraceId(JobKey jobKey);

}
