package com.github.paganini2008.springdessert.cluster.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.AtomicIntegerSequence;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

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
	public static class RoundRobinLoadBalancer implements LoadBalancer {

		private final AtomicIntegerSequence counter = new AtomicIntegerSequence();

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			return candidates.get(counter.getAndIncrement() % candidates.size());
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
	public static class RandomLoadBalancer implements LoadBalancer {

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			return candidates.get(ThreadLocalRandom.current().nextInt(0, candidates.size()));
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
	public static class HashLoadBalancer implements LoadBalancer {

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			int hash = message != null ? message.hashCode() : 0;
			hash &= 0x7FFFFFFF;
			return candidates.get(hash % candidates.size());
		}

	}

}
