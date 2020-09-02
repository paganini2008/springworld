package com.github.paganini2008.springworld.crontab.server;

import com.github.paganini2008.springworld.crontab.JobException;

/**
 * 
 * NoJobResourceException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class NoJobResourceException extends JobException {
	
	private static final long serialVersionUID = 3321347517738601224L;
	
	public NoJobResourceException(String clusterName) {
		super(clusterName);
	}

}
