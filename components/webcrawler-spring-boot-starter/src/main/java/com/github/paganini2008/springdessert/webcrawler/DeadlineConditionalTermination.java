package com.github.paganini2008.springdessert.webcrawler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.xtransport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DeadlineConditionalTermination
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Slf4j
public class DeadlineConditionalTermination extends AbstractConditionalTermination {

	private static final String defaultRedisKeyPrefix = "spring:application:cluster:%s:deadline:";
	private final String keyPrefix;
	private final long remaining;
	private final RedisTemplate<String, Long> redisTemplate;

	public DeadlineConditionalTermination(CrawlerSummary crawlerSummary, long delay, TimeUnit timeUnit,
			RedisConnectionFactory redisConnectionFactory) {
		super(crawlerSummary);
		this.keyPrefix = String.format(defaultRedisKeyPrefix, crawlerSummary.getCrawlerName());
		this.remaining = DateUtils.convertToMillis(delay, timeUnit);
		this.redisTemplate = getLong(redisConnectionFactory);
	}

	@Override
	public void reset(long catalogId) {
		super.reset(catalogId);
		String key = keyPrefix + catalogId;
		redisTemplate.delete(key);

	}

	@Override
	public boolean mightComplete(long catalogId, Tuple tuple) {
		if (isCompleted(catalogId, tuple)) {
			return true;
		}
		String key = keyPrefix + catalogId;
		if (!redisTemplate.hasKey(key)) {
			redisTemplate.opsForValue().set(key, System.currentTimeMillis() + remaining);
		}

		boolean completed = System.currentTimeMillis() > redisTemplate.opsForValue().get(key) || evaluate(catalogId, tuple);
		set(catalogId, completed);
		if (completed) {
			long timestamp = redisTemplate.opsForValue().get(key);
			log.info("Finish crawling work on deadline: {}", new Date(timestamp));
			afterCompletion(catalogId, tuple);
		}
		return isCompleted(catalogId, tuple);
	}

	public long getRemaining() {
		return remaining;
	}

	public RedisTemplate<String, Long> getRedisTemplate() {
		return redisTemplate;
	}

	protected boolean evaluate(long catalogId, Tuple tuple) {
		return false;
	}

	protected void afterCompletion(long catalogId, Tuple tuple) {
	}

	private RedisTemplate<String, Long> getLong(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));
		redisTemplate.setExposeConnection(true);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

}
