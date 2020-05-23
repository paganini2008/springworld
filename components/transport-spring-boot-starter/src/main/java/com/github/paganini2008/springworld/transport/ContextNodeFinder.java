package com.github.paganini2008.springworld.transport;

import static com.github.paganini2008.springworld.transport.Constants.APPLICATION_KEY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.Acceptable;
import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.springworld.cluster.ClusterId;
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

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public void registerNode(Object attachment) {
		final String key = String.format(APPLICATION_KEY, applicationName);
		final String instanceId = clusterId.get();
		redisTemplate.opsForHash().put(key, instanceId, attachment);
		log.info("Register node '{}' to spring application cluster: {}", instanceId, applicationName);
	}

	@Override
	public Object findNode(String instanceId) {
		log.info("Find node '{}' from spring application cluster: {}", instanceId, applicationName);
		final String key = String.format(APPLICATION_KEY, applicationName);
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
		return (String) redisTemplate.opsForHash().get(key, instanceId);
	}

}
