package com.github.paganini2008.transport.netty;

import java.nio.charset.Charset;

import io.netty.channel.ChannelHandler;
import io.netty.util.CharsetUtil;

/**
 * 
 * NettyJsonToTupleCodeFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NettyJsonToTupleCodeFactory implements MessageCodecFactory {

	private final Charset charset;
	
	public NettyJsonToTupleCodeFactory() {
		this(CharsetUtil.UTF_8);
	}

	public NettyJsonToTupleCodeFactory(Charset charset) {
		this.charset = charset;
	}

	public ChannelHandler getEncoder() {
		return new NettyEncoderDecoders.JsonEncoder(charset);
	}

	public ChannelHandler getDecoder() {
		return new NettyEncoderDecoders.JsonToTupleDecoder(charset);
	}

	public Charset getCharset() {
		return charset;
	}

}
