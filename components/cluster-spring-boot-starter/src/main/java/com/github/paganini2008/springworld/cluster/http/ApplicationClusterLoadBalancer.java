package com.github.paganini2008.springworld.cluster.http;

import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.utils.LoadBalancer;
import com.github.paganini2008.springworld.reditools.common.RedisAtomicLongSequence;

/**
 * 
 * ApplicationClusterLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ApplicationClusterLoadBalancer implements LoadBalancer<ApplicationInfo> {

	private final RedisAtomicLongSequence counter;

	public ApplicationClusterLoadBalancer(String name, RedisConnectionFactory connectionFactory) {
		this.counter = new RedisAtomicLongSequence(name, connectionFactory);
	}

	@Override
	public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
		if (CollectionUtils.isEmpty(candidates)) {
			return null;
		}
		return candidates.get((int) (counter.getAndIncrement() % candidates.size()));
	}

}
