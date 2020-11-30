package com.github.paganini2008.xtransport;

import java.util.Set;

import com.github.paganini2008.devtools.Console;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

/**
 * 
 * JedisPoolBuilder
 *
 * @author Fred Feng
 * @since 1.0
 */
public class JedisPoolBuilder implements RedisPoolBuilder<Pool<Jedis>> {

	private String host = "localhost";
	private int port = 6379;
	private String auth = "";
	private int dbIndex = 0;

	public String getHost() {
		return host;
	}

	public JedisPoolBuilder setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public JedisPoolBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public String getAuth() {
		return auth;
	}

	public JedisPoolBuilder setAuth(String auth) {
		this.auth = auth;
		return this;
	}

	public int getDbIndex() {
		return dbIndex;
	}

	public JedisPoolBuilder setDbIndex(int dbIndex) {
		this.dbIndex = dbIndex;
		return this;
	}

	@Override
	public Pool<Jedis> createPool() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(2);
		jedisPoolConfig.setMaxTotal(10);
		jedisPoolConfig.setMaxWaitMillis(-1);
		jedisPoolConfig.setTestWhileIdle(true);
		return new JedisPool(jedisPoolConfig, host, port, 60 * 1000, auth, dbIndex);
	}
	
	public static void main(String[] args) {
		JedisPoolBuilder poolBuilder = new JedisPoolBuilder();
		poolBuilder.setAuth("123456");
		Pool<Jedis> pool = poolBuilder.createPool();
		Jedis jedis = pool.getResource();
		Set<String> keys= jedis.keys("*");
		Console.log(keys);
	}

}
