package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.data.redis.core.RedisOperations;

import com.github.paganini2008.springdessert.reditools.common.RedisBloomFilter;

/**
 * 
 * BloomFilterPathFilterFactory
 *
 * @author Fred Feng
 * @since 1.0
 */
public class BloomFilterPathFilterFactory implements PathFilterFactory {

	private static final String redisKeyPrefix = "bloomFiter:";
	private static final int maxExpectedInsertions = 100000000;
	private final RedisOperations<String, String> redisOperations;

	public BloomFilterPathFilterFactory(RedisOperations<String, String> redisOperations) {
		this.redisOperations = redisOperations;
	}

	@Override
	public void clean(String identifier) {
		redisOperations.delete(redisKeyPrefix + identifier);
	}

	@Override
	public PathFilter getPathFilter(String identifier) {
		RedisBloomFilter bloomFilter = new RedisBloomFilter(redisKeyPrefix + identifier, maxExpectedInsertions, 0.03d, redisOperations);
		return new BloomFilterPathFilter(bloomFilter);
	}

}
