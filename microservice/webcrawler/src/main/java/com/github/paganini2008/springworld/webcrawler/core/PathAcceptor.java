package com.github.paganini2008.springworld.webcrawler.core;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * PathAcceptor
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface PathAcceptor {

	boolean accept(String refer, String path, Tuple tuple);

}
