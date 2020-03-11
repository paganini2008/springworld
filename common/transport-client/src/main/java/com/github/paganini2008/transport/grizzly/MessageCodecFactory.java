package com.github.paganini2008.transport.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Transformer;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * MessageCodecFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface MessageCodecFactory {

	Transformer<Tuple, Buffer> getEncoder();

	Transformer<Buffer, Tuple> getDecoder();

}