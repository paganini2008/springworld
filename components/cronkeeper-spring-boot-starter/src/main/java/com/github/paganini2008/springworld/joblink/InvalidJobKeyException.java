package com.github.paganini2008.springworld.joblink;

/**
 * 
 * InvalidJobKeyException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class InvalidJobKeyException extends JobException {

	private static final long serialVersionUID = 7466940014385117941L;

	public InvalidJobKeyException(String repr) {
		super(repr);
	}

	public InvalidJobKeyException(String repr, Throwable e) {
		super(repr, e);
	}

}
