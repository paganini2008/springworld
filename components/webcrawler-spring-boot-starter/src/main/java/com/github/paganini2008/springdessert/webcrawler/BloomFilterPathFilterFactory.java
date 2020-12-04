package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.data.redis.core.RedisOperations;

import com.github.paganini2008.springdessert.reditools.common.RedisBloomFilter;

/**
 * 
 * BloomFilterPathFilterFactory
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class BloomFilterPathFilterFactory implements PathFilterFactory {

	private static final String defaultRedisKeyPrefix = "spring:webcrawler:cluster:%s:catalog:bloomFiter:%s";
	private static final int maxExpectedInsertions = 100000000;
	private final RedisOperations<String, String> redisOperations;
	private final String crawlerName;

	public BloomFilterPathFilterFactory(String crawlerName, RedisOperations<String, String> redisOperations) {
		this.crawlerName = crawlerName;
		this.redisOperations = redisOperations;
	}

	@Override
	public void clean(long catalogId) {
		String key = String.format(defaultRedisKeyPrefix, crawlerName, catalogId);
		redisOperations.delete(key);
	}

	@Override
	public PathFilter getPathFilter(long catalogId) {
		String key = String.format(defaultRedisKeyPrefix, crawlerName, catalogId);
		RedisBloomFilter bloomFilter = new RedisBloomFilter(key, maxExpectedInsertions, 0.03d, redisOperations);
		return new BloomFilterPathFilter(bloomFilter);
	}

}
