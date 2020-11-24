package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * PathAcceptor
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface PathAcceptor {

	boolean accept(String refer, String path, Tuple tuple);

}
