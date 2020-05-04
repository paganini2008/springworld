package com.github.paganini2008.embeddedio;

/**
 * 
 * Channel
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Channel {

	long writeAndFlush(Object message);

	long write(Object message, int batchSize);

	long flush();

	long read();

	void close();

	boolean isActive();

}