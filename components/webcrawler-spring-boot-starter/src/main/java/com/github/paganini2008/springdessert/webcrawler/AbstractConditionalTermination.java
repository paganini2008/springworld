package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * AbstractConditionalTermination
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public abstract class AbstractConditionalTermination implements ConditionalTermination {

	private final CrawlerSummary crawlerSummary;

	protected AbstractConditionalTermination(CrawlerSummary crawlerSummary) {
		this.crawlerSummary = crawlerSummary;
	}

	@Override
	public void reset(long catalogId) {
		crawlerSummary.reset(catalogId);
	}

	protected void set(long catalogId, boolean completed) {
		crawlerSummary.getSummary(catalogId).setCompleted(completed);
	}

	@Override
	public boolean isCompleted(long catalogId, Tuple tuple) {
		return crawlerSummary.getSummary(catalogId).isCompleted();
	}

	public CrawlerSummary getCrawlerSummary() {
		return crawlerSummary;
	}

}
