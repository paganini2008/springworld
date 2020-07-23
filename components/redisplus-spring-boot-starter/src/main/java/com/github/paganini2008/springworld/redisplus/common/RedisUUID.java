package com.github.paganini2008.springworld.redisplus.common;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

/**
 * 
 * RedisUUID
 *
 * @author Fred Feng
 * @since 1.0
 */
public class RedisUUID {

	private static final long seed = 0x01B21DD213814000L;
	private static final long leastSigBits;

	static {
		byte[] seed = new SecureRandom().generateSeed(8);
		leastSigBits = new BigInteger(seed).longValue();
	}

	public RedisUUID(String redisCounterName, RedisOperations<String, Long> redisOperations) {
		this.lastTime = new RedisAtomicLong(redisCounterName, redisOperations);
	}

	public RedisUUID(String redisCounterName, RedisConnectionFactory redisConnectionFactory) {
		this.lastTime = new RedisAtomicLong(redisCounterName, redisConnectionFactory);
	}

	private final RedisAtomicLong lastTime;

	public UUID createUUID() {
		long timeMillis = (System.currentTimeMillis() * 10000) + seed;
		long lastTimeVal = lastTime.get();
		if (timeMillis > lastTimeVal) {
			lastTime.set(lastTimeVal);
		} else {
			timeMillis = lastTime.incrementAndGet();
		}
		long mostSigBits = timeMillis << 32;
		mostSigBits |= (timeMillis & 0xFFFF00000000L) >> 16;
		mostSigBits |= 0x1000 | ((timeMillis >> 48) & 0x0FFF);
		return new UUID(mostSigBits, leastSigBits);
	}
}
