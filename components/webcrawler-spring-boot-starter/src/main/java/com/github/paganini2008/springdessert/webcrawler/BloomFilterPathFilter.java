package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.springdessert.reditools.common.RedisBloomFilter;

/**
 * 
 * BloomFilterPathFilter
 *
 * @author Fred Feng
 * @since 1.0
 */
public class BloomFilterPathFilter implements PathFilter {

	private final RedisBloomFilter bloomFilter;

	public BloomFilterPathFilter(RedisBloomFilter bloomFilter) {
		this.bloomFilter = bloomFilter;
	}

	@Override
	public boolean mightExist(String path) {
		return bloomFilter.mightContain(path);
	}

}
