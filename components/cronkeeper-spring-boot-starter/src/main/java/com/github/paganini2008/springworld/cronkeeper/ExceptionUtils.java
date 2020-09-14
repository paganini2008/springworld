package com.github.paganini2008.springworld.cronkeeper;

/**
 * 
 * ExceptionUtils
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class ExceptionUtils {

	public static JobException wrapExeception(Throwable e) {
		return e instanceof JobException ? (JobException) e : new JobException(e.getMessage(), e);
	}

}
