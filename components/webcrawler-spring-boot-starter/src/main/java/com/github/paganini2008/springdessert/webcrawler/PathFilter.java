package com.github.paganini2008.springdessert.webcrawler;

/**
 * 
 * PathFilter
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface PathFilter {
	
	void update(String content);

	boolean mightExist(String content);

}
