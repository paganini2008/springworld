package com.github.paganini2008.transport.grizzly;

import com.github.paganini2008.transport.grizzly.GrizzlyEncoderDecoders.TupleDecoder;
import com.github.paganini2008.transport.grizzly.GrizzlyEncoderDecoders.TupleEncoder;
import com.github.paganini2008.transport.serializer.KryoSerializer;
import com.github.paganini2008.transport.serializer.Serializer;

/**
 * 
 * GrizzlyTupleCodecFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class GrizzlyTupleCodecFactory implements TupleCodecFactory {

	private final Serializer serializer;
	
	public GrizzlyTupleCodecFactory() {
		this(new KryoSerializer());
	}

	public GrizzlyTupleCodecFactory(Serializer serializer) {
		this.serializer = serializer;
	}

	public TupleEncoder getEncoder() {
		return new TupleEncoder(serializer);
	}

	public TupleDecoder getDecoder() {
		return new TupleDecoder(serializer);
	}

	public Serializer getSerializer() {
		return serializer;
	}

}
