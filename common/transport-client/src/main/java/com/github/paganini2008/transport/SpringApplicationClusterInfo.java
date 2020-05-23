package com.github.paganini2008.transport;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.util.Pool;

/**
 * 
 * SpringApplicationClusterInfo
 *
 * @author Fred Feng
 * @since 1.0
 */
public class SpringApplicationClusterInfo extends CachedClusterInfo<JedisPool> {

	public SpringApplicationClusterInfo(String applicationName) {
		super(applicationName);
	}

	private JedisPoolBuilder builder = new JedisPoolBuilder();
	private Pool<Jedis> pool;

	public JedisPoolBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(JedisPoolBuilder builder) {
		this.builder = builder;
	}

	@Override
	protected JedisCommands getJedisOperations() {
		if (pool == null) {
			pool = builder.createPool();
		}
		return pool.getResource();
	}

	@Override
	protected void releaseJedis(JedisCommands jedisCommands) {
		((Jedis) jedisCommands).close();
	}

	@Override
	public void releaseExternalResources() {
		if (pool != null) {
			pool.close();
		}
	}

}
