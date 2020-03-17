package com.github.paganini2008.transport;

import java.util.ArrayList;
import java.util.List;

import com.github.paganini2008.devtools.collection.CollectionUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * JedisLookupAddress
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JedisLookupAddress implements LookupAddress {

	private final String host;
	private final int port;
	private final String password;
	private final int dbIndex;

	public JedisLookupAddress(String host, int port, String password, int dbIndex) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.dbIndex = dbIndex;
	}

	private JedisPool jedisPool;

	@Override
	public String[] getAddresses(String clusterName) throws Exception {
		List<String> addresses = new ArrayList<String>();
		Jedis jedis = null;
		try {
			if (jedisPool == null) {
				jedisPool = getPool();
			}
			jedis = jedisPool.getResource();
			String fullName = CLUSTER_NAMESPACE + clusterName;
			List<String> clusterIds = jedis.lrange(fullName, 0, -1);
			if (CollectionUtils.isNotEmpty(clusterIds)) {
				String key = APPLICATION_KEY_PREFIX + clusterName;
				addresses.addAll(jedis.hmget(key, clusterIds.toArray(new String[0])));
			}
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return addresses.toArray(new String[0]);
	}

	@Override
	public void releaseExternalResources() {
		if (jedisPool != null) {
			jedisPool.close();
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}

	protected JedisPool getPool() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(2);
		jedisPoolConfig.setMaxTotal(10);
		jedisPoolConfig.setMaxWaitMillis(-1);
		jedisPoolConfig.setTestWhileIdle(true);
		return new JedisPool(jedisPoolConfig, host, port, 60 * 1000, password, dbIndex);
	}

}
