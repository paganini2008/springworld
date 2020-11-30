package com.github.paganini2008.xtransport;

import java.util.List;

import com.github.paganini2008.devtools.multithreads.AtomicLongSequence;

/**
 * 
 * RoundRobinPartitioner
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RoundRobinPartitioner implements Partitioner {

	private final AtomicLongSequence sequence = new AtomicLongSequence();

	public <T> T selectChannel(Object data, List<T> channels) {
		try {
			int index = (int) (sequence.incrementAndGet() % channels.size());
			return channels.get(index);
		} catch (RuntimeException e) {
			return null;
		}
	}

}
