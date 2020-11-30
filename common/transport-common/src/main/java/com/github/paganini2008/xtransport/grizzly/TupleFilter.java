package com.github.paganini2008.xtransport.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;

import com.github.paganini2008.xtransport.Tuple;
import com.github.paganini2008.xtransport.serializer.KryoSerializer;
import com.github.paganini2008.xtransport.serializer.Serializer;

/**
 * 
 * TupleFilter
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TupleFilter extends AbstractCodecFilter<Buffer, Tuple> {

	public TupleFilter() {
		this(new KryoSerializer());
	}

	public TupleFilter(Serializer serializer) {
		this(new GrizzlyTupleCodecFactory(serializer));
	}

	public TupleFilter(TupleCodecFactory codecFactory) {
		super(codecFactory.getDecoder(), codecFactory.getEncoder());
	}

}
