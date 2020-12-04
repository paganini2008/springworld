package com.github.paganini2008.springdessert.webcrawler;

/**
 * 
 * PathFilterFactory
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface PathFilterFactory {

	void clean(long catalogId);

	PathFilter getPathFilter(long catalogId);

}
