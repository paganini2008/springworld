package com.github.paganini2008.transport.grizzly;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;

import com.github.paganini2008.transport.TransportClientException;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.serializer.Serializer;

/**
 * 
 * GrizzlyEncoderDecoders
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class GrizzlyEncoderDecoders {

	public static class TupleDecoder extends AbstractTransformer<Buffer, Tuple> {

		private final Serializer serializer;

		public TupleDecoder(Serializer serializer) {
			this.serializer = serializer;
		}

		@Override
		public String getName() {
			return "TupleDecoder";
		}

		@Override
		public boolean hasInputRemaining(AttributeStorage storage, Buffer input) {
			return input != null && input.hasRemaining();
		}

		@Override
		protected TransformationResult<Buffer, Tuple> transformImpl(AttributeStorage storage, Buffer input) throws TransformationException {
			if (input.remaining() < 4) {
				return TransformationResult.createIncompletedResult(input);
			}
			int dataLength = input.getInt();
			if (dataLength < 4) {
				throw new TransportClientException("Data length should be greater than 4: " + dataLength);
			}
			if (input.remaining() < dataLength) {
				return TransformationResult.createIncompletedResult(input);
			}
			byte[] data = new byte[dataLength];
			input.get(data);
			Tuple tuple = serializer.deserialize(data);
			return TransformationResult.createCompletedResult(tuple, input);
		}

	}
	
	public static class TupleEncoder extends AbstractTransformer<Tuple, Buffer> {

		private final Serializer serializer;

		public TupleEncoder(Serializer serializer) {
			this.serializer = serializer;
		}

		@Override
		public String getName() {
			return "TupleEncoder";
		}

		@Override
		public boolean hasInputRemaining(AttributeStorage storage, Tuple input) {
			return input != null;
		}

		@Override
		protected TransformationResult<Tuple, Buffer> transformImpl(AttributeStorage storage, Tuple input) throws TransformationException {
			if (input == null) {
				throw new TransportClientException("Input could not be null");
			}
			byte[] data = serializer.serialize(input);

			final Buffer output = obtainMemoryManager(storage).allocate(data.length + 4);
			output.putInt(data.length);
			output.put(data);
			output.flip();
			output.allowBufferDispose(true);

			return TransformationResult.createCompletedResult(output, null);
		}

	}
	
}
