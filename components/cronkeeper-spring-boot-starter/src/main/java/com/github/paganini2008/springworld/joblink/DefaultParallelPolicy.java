package com.github.paganini2008.springworld.joblink;

import java.util.Collection;

/**
 * 
 * DefaultParallelPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DefaultParallelPolicy implements ParallelPolicy {

	@Override
	public Object[] slice(Object attachment) {
		if (attachment instanceof CharSequence) {
			return ((CharSequence) attachment).toString().split(",");
		} else if (attachment instanceof Object[]) {
			return (Object[]) attachment;
		} else if (attachment instanceof Collection<?>) {
			return ((Collection<?>) attachment).toArray();
		}
		return new Object[] { attachment };
	}

}
