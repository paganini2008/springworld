package com.github.paganini2008.springdessert.webcrawler;

/**
 * 
 * PathFilterFactory
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface PathFilterFactory {

	void clean(String identifier);

	PathFilter getPathFilter(String identifier);

}
