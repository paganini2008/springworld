package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * CountingCondition
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class CountingCondition extends DurationCondition {

	public CountingCondition(CrawlerSummary crawlerSummary, long defaultDuration, int defaultMaxFetchSize) {
		super(crawlerSummary, defaultDuration);
		this.defaultMaxFetchSize = defaultMaxFetchSize;
	}

	private final int defaultMaxFetchSize;

	private ConditionalCountingType countingType = ConditionalCountingType.URL_COUNT;

	public void setCountingType(ConditionalCountingType countingType) {
		this.countingType = countingType;
	}

	@Override
	protected boolean evaluate(long catalogId, Tuple tuple) {
		long maxFetchSize = (Integer) tuple.getField("maxFetchSize", defaultMaxFetchSize);
		return countingType.evaluate(getCrawlerSummary().getSummary(catalogId), maxFetchSize);
	}

}
