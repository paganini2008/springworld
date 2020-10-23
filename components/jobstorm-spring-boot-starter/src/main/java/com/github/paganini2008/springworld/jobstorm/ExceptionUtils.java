package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * ExceptionUtils
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class ExceptionUtils {

	public static JobException wrapExeception(String msg, Throwable e) {
		return e instanceof JobException ? (JobException) e : new JobException(msg, e);
	}

	public static JobException wrapExeception(Throwable e) {
		return wrapExeception(e.getMessage(), e);
	}

}
