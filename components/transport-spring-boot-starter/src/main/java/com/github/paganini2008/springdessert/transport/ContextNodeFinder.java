package com.github.paganini2008.springdessert.transport;

import static com.github.paganini2008.transport.Constants.APPLICATION_KEY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.Acceptable;
import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.transport.NodeFinder;
import com.github.paganini2008.transport.TransportClientException;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ContextNodeFinder
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ContextNodeFinder implements NodeFinder {

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Autowired
	private InstanceId clusterId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public void registerNode(Object attachment) {
		final String key = APPLICATION_KEY + clusterName;
		final String instanceId = clusterId.get();
		redisTemplate.opsForHash().put(key, instanceId, attachment);
		log.info("Register node '{}' to spring application cluster '{}'", instanceId, clusterName);
	}

	@Override
	public Object findNode(String instanceId) {
		log.info("Find node '{}' from spring application cluster '{}'", instanceId, clusterName);
		final String key = APPLICATION_KEY + clusterName;
		if (!ObjectUtils.accept(new Acceptable() {

			@Override
			public boolean accept() {
				return redisTemplate.opsForHash().hasKey(key, instanceId);
			}

			@Override
			public int retries() {
				return 10;
			}

		})) {
			throw new TransportClientException("InstanceId not found!");
		}
		String location = (String) redisTemplate.opsForHash().get(key, instanceId);
		System.out.println("InstanceId: " + instanceId + ", Location: " + location);
		return location;
	}

}
