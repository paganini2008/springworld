package com.github.paganini2008.springdessert.transport;

import static com.github.paganini2008.transport.Constants.SPRING_TRANSPORT_CLUSTER_NAMESPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.devtools.Retryable;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.transport.TransportClientException;
import com.github.paganini2008.transport.TransportNodeCentre;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultTransportNodeCentre
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class DefaultTransportNodeCentre implements TransportNodeCentre {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public void registerNode(Object attachment) {
		final String key = String.format(SPRING_TRANSPORT_CLUSTER_NAMESPACE, clusterName);
		final String id = instanceId.get();
		redisTemplate.opsForHash().put(key, id, attachment);
		log.info("Register node '{}' to spring transport cluster '{}'", id, clusterName);
	}

	@Override
	public Object findNode(final String instanceId) {
		log.info("Find node '{}' from spring transport cluster '{}'", instanceId, clusterName);
		final String key = String.format(SPRING_TRANSPORT_CLUSTER_NAMESPACE, clusterName);
		return ObjectUtils.polling(new Retryable<String>() {

			@Override
			public boolean tryAccept() {
				return redisTemplate.opsForHash().hasKey(key, instanceId);
			}

			@Override
			public String accept() {
				return (String) redisTemplate.opsForHash().get(key, instanceId);
			}

			@Override
			public int maxAttempts() {
				return 10;
			}

		}, () -> {
			throw new TransportClientException("TransportNode: " + instanceId + " can not be found!");
		});
	}

}
