package com.github.paganini2008.transport.netty;

import com.github.paganini2008.transport.serializer.KryoSerializer;
import com.github.paganini2008.transport.serializer.Serializer;

import io.netty.channel.ChannelHandler;

/**
 * 
 * TupleCodecFactory
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class NettyTupleCodecFactory implements MessageCodecFactory {

	private final Serializer serializer;

	public NettyTupleCodecFactory() {
		this(new KryoSerializer());
	}

	public NettyTupleCodecFactory(Serializer serializer) {
		this.serializer = serializer;
	}

	public ChannelHandler getEncoder() {
		return new NettyEncoderDecoders.TupleEncoder(serializer);
	}

	public ChannelHandler getDecoder() {
		return new NettyEncoderDecoders.TupleDecoder(serializer);
	}

	public Serializer getSerializer() {
		return serializer;
	}

}
