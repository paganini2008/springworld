package com.github.paganini2008.springdessert.webcrawler;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * CountingConditionalMission
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class CountingConditionalMission extends DeadlineConditionalMission {

	public CountingConditionalMission(String keyPrefix, RedisConnectionFactory redisConnectionFactory, long delay, TimeUnit timeUnit,
			long maxFetchSize, CrawlerSummary crawlerSummary) {
		super(keyPrefix, redisConnectionFactory, delay, timeUnit);
		Assert.lt(maxFetchSize, 100L, "Minimun maxFetchSize is 100");
		this.maxFetchSize = maxFetchSize;
		this.crawlerSummary = crawlerSummary;
	}

	private final long maxFetchSize;
	private final CrawlerSummary crawlerSummary;

	private ConditionalCountingType countingType = ConditionalCountingType.URL_COUNT;

	public void setCountingType(ConditionalCountingType countingType) {
		this.countingType = countingType;
	}

	@Override
	protected boolean evaluate(long catalogId, Tuple tuple) {
		return countingType.evaluate(crawlerSummary.getSummary(catalogId), maxFetchSize);
	}

}
