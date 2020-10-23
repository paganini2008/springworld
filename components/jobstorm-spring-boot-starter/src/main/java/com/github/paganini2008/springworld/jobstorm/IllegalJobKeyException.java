package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * IllegalJobKeyException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class IllegalJobKeyException extends JobException {

	private static final long serialVersionUID = 7466940014385117941L;

	public IllegalJobKeyException(String repr) {
		super(repr);
	}

	public IllegalJobKeyException(String repr, Throwable e) {
		super(repr, e);
	}

}
