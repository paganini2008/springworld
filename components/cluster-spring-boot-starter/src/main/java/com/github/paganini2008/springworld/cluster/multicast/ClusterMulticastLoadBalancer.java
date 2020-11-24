package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.springdessert.reditools.common.RedisAtomicLongSequence;
import com.github.paganini2008.springworld.cluster.utils.LoadBalancer;

/**
 * 
 * ClusterMulticastLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ClusterMulticastLoadBalancer implements LoadBalancer<String> {

	private final RedisAtomicLongSequence counter;

	public ClusterMulticastLoadBalancer(String name, RedisConnectionFactory connectionFactory) {
		this.counter = new RedisAtomicLongSequence(name, connectionFactory);
	}

	@Override
	public String select(Object message, List<String> channels) {
		if (CollectionUtils.isEmpty(channels)) {
			return null;
		}
		return channels.get((int) (counter.getAndIncrement() % channels.size()));
	}

}
