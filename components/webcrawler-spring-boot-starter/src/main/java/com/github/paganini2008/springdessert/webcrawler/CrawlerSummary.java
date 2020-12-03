package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * CrawlerSummary
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class CrawlerSummary implements DisposableBean {

	private static final String defaultRedisKeyPattern = "spring:application:cluster:%s:summary:%s";

	private final String crawlerName;
	private final RedisConnectionFactory redisConnectionFactory;

	public CrawlerSummary(String crawlerName, RedisConnectionFactory redisConnectionFactory) {
		this.crawlerName = crawlerName;
		this.redisConnectionFactory = redisConnectionFactory;
	}

	private final Map<Long, Summary> cache = new ConcurrentHashMap<Long, Summary>();

	public void reset(long catalogId) {
		getSummary(catalogId).reset();
	}

	public Summary getSummary(long catalogId) {
		final String key = String.format(defaultRedisKeyPattern, crawlerName, catalogId);
		return MapUtils.get(cache, catalogId, () -> {
			return new Summary(key, redisConnectionFactory);
		});
	}

	public String getCrawlerName() {
		return crawlerName;
	}

	@Override
	public void destroy() throws Exception {
		cache.values().forEach(summary -> {
			summary.reset();
		});
		cache.clear();
	}

	public static class Summary {

		private final RedisAtomicLong urls;
		private final RedisAtomicLong invalidUrls;
		private final RedisAtomicLong existedUrls;
		private final RedisAtomicLong filteredUrls;
		private final RedisAtomicLong saved;
		private final RedisAtomicLong indexed;
		private long startTime;
		private boolean completed;

		Summary(String keyPrefix, RedisConnectionFactory redisConnectionFactory) {
			startTime = System.currentTimeMillis();
			urls = new RedisAtomicLong(keyPrefix + ":urls", redisConnectionFactory);
			invalidUrls = new RedisAtomicLong(keyPrefix + ":invalidUrls", redisConnectionFactory);
			existedUrls = new RedisAtomicLong(keyPrefix + ":existedUrls", redisConnectionFactory);
			filteredUrls = new RedisAtomicLong(keyPrefix + ":filteredUrls", redisConnectionFactory);
			saved = new RedisAtomicLong(keyPrefix + ":saved", redisConnectionFactory);
			indexed = new RedisAtomicLong(keyPrefix + ":indexed", redisConnectionFactory);
			completed = false;
		}

		public void reset() {
			startTime = System.currentTimeMillis();
			urls.set(0);
			invalidUrls.set(0);
			existedUrls.set(0);
			filteredUrls.set(0);
			saved.set(0);
			indexed.set(0);
		}

		public boolean isCompleted() {
			return completed;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public long incrementUrlCount() {
			return urls.incrementAndGet();
		}

		public long incrementInvalidUrlCount() {
			return invalidUrls.incrementAndGet();
		}

		public long incrementExistedUrlCount() {
			return existedUrls.incrementAndGet();
		}

		public long incrementFilteredUrlCount() {
			return filteredUrls.incrementAndGet();
		}

		public long incrementSavedCount() {
			return saved.incrementAndGet();
		}

		public long incrementIndexedCount() {
			return indexed.incrementAndGet();
		}

		public long incrementUrlCount(int delta) {
			return urls.addAndGet(delta);
		}

		public long incrementInvalidUrlCount(int delta) {
			return invalidUrls.addAndGet(delta);
		}

		public long incrementExistedUrlCount(int delta) {
			return existedUrls.addAndGet(delta);
		}

		public long incrementFilteredUrlCount(int delta) {
			return filteredUrls.addAndGet(delta);
		}

		public long incrementSavedCount(int delta) {
			return saved.addAndGet(delta);
		}

		public long incrementIndexedCount(int delta) {
			return indexed.addAndGet(delta);
		}

		public long getUrlCount() {
			return urls.get();
		}

		public long getInvalidUrlCount() {
			return invalidUrls.get();
		}

		public long getExistedUrlCount() {
			return existedUrls.get();
		}

		public long getFilteredUrlCount() {
			return filteredUrls.get();
		}

		public long getSavedCount() {
			return saved.get();
		}

		public long getIndexedCount() {
			return indexed.get();
		}

		public long getStartTime() {
			return startTime;
		}

		public long getElapsedTime() {
			return System.currentTimeMillis() - startTime;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

}
