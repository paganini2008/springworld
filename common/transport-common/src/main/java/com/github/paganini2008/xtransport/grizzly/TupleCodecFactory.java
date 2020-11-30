package com.github.paganini2008.xtransport.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Transformer;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * TupleCodecFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TupleCodecFactory {

	Transformer<Tuple, Buffer> getEncoder();

	Transformer<Buffer, Tuple> getDecoder();

}