package com.github.paganini2008.transport;

import java.util.ArrayList;
import java.util.List;

import com.github.paganini2008.devtools.StringUtils;

import redis.clients.jedis.commands.JedisCommands;

/**
 * 
 * CachedClusterInfo
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class CachedClusterInfo<T> implements ClusterInfo {

	private final String applicationName;

	protected CachedClusterInfo(String applicationName) {
		if (StringUtils.isBlank(applicationName)) {
			throw new TransportClientException("ClusterName must be required.");
		}
		this.applicationName = applicationName;
	}

	protected abstract JedisCommands getJedisOperations();

	protected abstract void releaseJedis(JedisCommands jedisCommands);

	@Override
	public String getName() {
		return applicationName;
	}

	@Override
	public String[] getInstanceIds() throws Exception {
		final String fullName = SPRING_CLUSTER_NAMESPACE + applicationName;
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = getJedisOperations();
			List<String> instanceIds = jedisCommands.lrange(fullName, 0, -1);
			return instanceIds != null ? instanceIds.toArray(new String[0]) : new String[0];
		} finally {
			if (jedisCommands != null) {
				releaseJedis(jedisCommands);
			}
		}
	}

	@Override
	public int getInstanceCount() throws Exception {
		final String fullName = SPRING_CLUSTER_NAMESPACE + applicationName;
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = getJedisOperations();
			Number result = jedisCommands.llen(fullName);
			return result != null ? result.intValue() : 0;
		} finally {
			if (jedisCommands != null) {
				releaseJedis(jedisCommands);
			}
		}
	}

	@Override
	public String[] getInstanceAddresses() throws Exception {
		List<String> addresses = new ArrayList<String>();
		String[] instanceIds = getInstanceIds();
		if (instanceIds != null && instanceIds.length > 0) {
			JedisCommands jedisCommands = null;
			try {
				String key = APPLICATION_KEY_PREFIX + applicationName;
				jedisCommands = getJedisOperations();
				addresses.addAll(jedisCommands.hmget(key, instanceIds));
			} finally {
				if (jedisCommands != null) {
					releaseJedis(jedisCommands);
				}
			}
		}
		return addresses.toArray(new String[0]);
	}

}
