package com.github.paganini2008.springworld.cronkeeper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;

import com.github.paganini2008.devtools.proxy.Aspect;

/**
 * 
 * JobLoggerAspect
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobLoggerAspect implements Aspect {

	private static final List<String> enhancedMethodNames = Collections
			.unmodifiableList(Arrays.asList("trace", "debug", "info", "warn", "error"));

	private final JobKey jobKey;
	private final LogManager logManager;

	JobLoggerAspect(JobKey jobKey, LogManager logManager) {
		this.jobKey = jobKey;
		this.logManager = logManager;
	}

	private long traceId;

	public void setTraceId(long traceId) {
		this.traceId = traceId;
	}

	@Override
	public boolean afterCall(Object target, Method method, Object[] args) {
		final Logger log = (Logger) target;
		final String methodName = method.getName();
		if (enhancedMethodNames.contains(methodName)) {
			List<Object> list = new ArrayList<Object>(Arrays.asList(args));
			Object firstArg = list.remove(0);
			Marker marker = firstArg instanceof Marker ? (Marker) firstArg : null;
			LogLevel logLevel = LogLevel.valueOf(methodName.toUpperCase());
			boolean canLog = marker != null ? logLevel.canLog(log, marker) : logLevel.canLog(log);
			if (canLog) {
				String messagePattern = marker != null ? (String) list.remove(1) : (String) firstArg;
				Throwable cause = null;
				if (list.size() > 0) {
					Object lastArg = list.get(list.size() - 1);
					if (lastArg instanceof Throwable) {
						cause = (Throwable) lastArg;
						list.remove(list.size() - 1);
					}
				}
				logManager.log(traceId, jobKey, logLevel, messagePattern, args, cause);
			}
		}
		return true;
	}

}
