package com.github.paganini2008.springworld.cluster;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.AtomicIntegerSequence;

/**
 * 
 * LoadBalancerUtils
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class LoadBalancerUtils {

	/**
	 * 
	 * RoundRobinLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public static class RoundRobinLoadBalancer implements LoadBalancer<String> {

		private final AtomicIntegerSequence counter = new AtomicIntegerSequence();

		public String select(Object message, List<String> channels) {
			if (CollectionUtils.isEmpty(channels)) {
				return null;
			}
			return channels.get(counter.getAndIncrement() % channels.size());
		}

	}

	/**
	 * 
	 * RandomLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public static class RandomLoadBalancer implements LoadBalancer<String> {

		public String select(Object message, List<String> channels) {
			if (CollectionUtils.isEmpty(channels)) {
				return null;
			}
			return channels.get(ThreadLocalRandom.current().nextInt(0, channels.size()));
		}

	}

	/**
	 * 
	 * HashLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public static class HashLoadBalancer implements LoadBalancer<String> {

		public String select(Object message, List<String> channels) {
			if (CollectionUtils.isEmpty(channels)) {
				return null;
			}
			int hash = message != null ? message.hashCode() : 0;
			hash &= 0x7FFFFFFF;
			return channels.get(hash % channels.size());
		}

	}

}
