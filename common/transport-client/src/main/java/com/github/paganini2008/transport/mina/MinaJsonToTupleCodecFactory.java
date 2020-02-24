package com.github.paganini2008.transport.mina;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.transport.mina.MinaEncoderDecoders.JsonEncoder;
import com.github.paganini2008.transport.mina.MinaEncoderDecoders.JsonToTupleDecoder;

/**
 * 
 * MinaJsonToTupleCodecFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class MinaJsonToTupleCodecFactory implements ProtocolCodecFactory {

	private final JsonEncoder encoder;
	private final JsonToTupleDecoder decoder;

	public MinaJsonToTupleCodecFactory() {
		this(CharsetUtils.UTF_8);
	}

	public MinaJsonToTupleCodecFactory(Charset charset) {
		encoder = new JsonEncoder(charset);
		decoder = new JsonToTupleDecoder(charset);
	}

	public ProtocolEncoder getEncoder(IoSession session) {
		return encoder;
	}

	public ProtocolDecoder getDecoder(IoSession session) {
		return decoder;
	}

	public int getEncoderMaxTextLength() {
		return encoder.getMaxTextLength();
	}

	public void setEncoderMaxTextLength(int maxTextLength) {
		encoder.setMaxTextLength(maxTextLength);
	}

	public int getDecoderMaxTextLength() {
		return decoder.getMaxTextLength();
	}

	public void setDecoderMaxTextLength(int maxTextLength) {
		decoder.setMaxTextLength(maxTextLength);
	}
}
