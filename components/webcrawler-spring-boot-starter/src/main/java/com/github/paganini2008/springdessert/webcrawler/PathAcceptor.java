package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * PathAcceptor
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface PathAcceptor {

	boolean accept(long catalogId, String refer, String path, Tuple tuple);

}
