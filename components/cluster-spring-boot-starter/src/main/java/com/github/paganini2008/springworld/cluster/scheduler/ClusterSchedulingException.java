package com.github.paganini2008.springworld.cluster.scheduler;

/**
 * 
 * ClusterSchedulingException
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ClusterSchedulingException extends RuntimeException {

	private static final long serialVersionUID = -7523610934014132232L;

	public ClusterSchedulingException() {
		super();
	}

	public ClusterSchedulingException(String msg) {
		super(msg);
	}

	public ClusterSchedulingException(String msg, Throwable e) {
		super(msg, e);
	}

}
