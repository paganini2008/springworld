package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.AtomicUnsignedInteger;

/**
 * 
 * PreparedLoadBalancer
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class PreparedLoadBalancer {

	public static class RoundRobinLoadBalance implements LoadBalance {

		private final AtomicUnsignedInteger counter = new AtomicUnsignedInteger();

		public String select(Object message, List<String> channels) {
			if (CollectionUtils.isEmpty(channels)) {
				return null;
			}
			return channels.get(counter.getAndIncrement() % channels.size());
		}

	}

	public static class RandomLoadBalance implements LoadBalance {

		public String select(Object message, List<String> channels) {
			if (CollectionUtils.isEmpty(channels)) {
				return null;
			}
			return channels.get(ThreadLocalRandom.current().nextInt(0, channels.size()));
		}

	}

	public static class HashLoadBalance implements LoadBalance {

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
