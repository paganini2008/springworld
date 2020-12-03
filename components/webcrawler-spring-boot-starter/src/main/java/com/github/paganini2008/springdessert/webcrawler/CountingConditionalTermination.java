package com.github.paganini2008.springdessert.webcrawler;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * CountingConditionalTermination
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class CountingConditionalTermination extends DeadlineConditionalTermination {

	public CountingConditionalTermination(CrawlerSummary crawlerSummary, long delay, TimeUnit timeUnit,
			RedisConnectionFactory redisConnectionFactory, long maxFetchSize) {
		super(crawlerSummary, delay, timeUnit, redisConnectionFactory);
		Assert.lt(maxFetchSize, 100L, "Minimun maxFetchSize is 100");
		this.maxFetchSize = maxFetchSize;
	}

	private final long maxFetchSize;

	private ConditionalCountingType countingType = ConditionalCountingType.URL_COUNT;

	public void setCountingType(ConditionalCountingType countingType) {
		this.countingType = countingType;
	}

	@Override
	protected boolean evaluate(long catalogId, Tuple tuple) {
		return countingType.evaluate(getCrawlerSummary().getSummary(catalogId), maxFetchSize);
	}

}
