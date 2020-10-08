package com.github.paganini2008.springworld.joblink;

/**
 * 
 * ParallelPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ParallelPolicy {

	Object[] slice(Object attachment);

}
