package com.github.paganini2008.transport.mina;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.serializer.Serializer;

/**
 * 
 * MinaEncoderDecoders
 * 
 * @author Fred Feng
 * @version 1.0
 */
public abstract class MinaEncoderDecoders {

	public static class JsonEncoder extends ProtocolEncoderAdapter {

		private final CharsetEncoder charsetEncoder;
		private int maxTextLength = Integer.MAX_VALUE;

		public JsonEncoder(Charset charset) {
			this.charsetEncoder = charset.newEncoder();
		}

		public CharsetEncoder getCharsetEncoder() {
			return charsetEncoder;
		}

		public int getMaxTextLength() {
			return maxTextLength;
		}

		public void setMaxTextLength(int maxTextLength) {
			this.maxTextLength = maxTextLength;
		}

		@Override
		public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
			String value = message instanceof CharSequence ? "" : message.toString();
			IoBuffer buf = IoBuffer.allocate(value.length() + 4).setAutoExpand(true);
			buf.putInt(value.length());
			buf.putString(value, charsetEncoder);
			if (buf.position() - 4 > maxTextLength) {
				throw new IllegalArgumentException("Line length: " + buf.position());
			}
			buf.flip();
			out.write(buf);
		}

	}

	public static class JsonToTupleDecoder extends CumulativeProtocolDecoder {

		private final ObjectMapper objectMapper = new ObjectMapper();
		private int maxTextLength = 8 * 1024 * 1024;
		private final CharsetDecoder charsetDecoder;

		public JsonToTupleDecoder(Charset charset) {
			this.charsetDecoder = charset.newDecoder();
		}

		public CharsetDecoder getCharsetDecoder() {
			return charsetDecoder;
		}

		public int getMaxTextLength() {
			return maxTextLength;
		}

		public void setMaxTextLength(int maxTextLength) {
			if (maxTextLength <= 0) {
				throw new IllegalArgumentException("maxTextLength: " + maxTextLength);
			}
			this.maxTextLength = maxTextLength;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
			if (!in.prefixedDataAvailable(4, maxTextLength)) {
				return false;
			}
			int length = in.getInt();
			if (length <= 4) {
				throw new BufferDataException("Object length should be greater than 4: " + length);
			}
			String str = in.getString(charsetDecoder);
			Map<String, Object> map = objectMapper.readValue(str, HashMap.class);
			Tuple tuple = Tuple.wrap(map);
			out.write(tuple);
			return true;
		}

	}

	public static class TupleEncoder extends ProtocolEncoderAdapter {
		private final Serializer serializer;
		private int maxObjectSize = Integer.MAX_VALUE;

		public TupleEncoder(Serializer serializer) {
			this.serializer = serializer;
		}

		public int getMaxObjectSize() {
			return maxObjectSize;
		}

		public void setMaxObjectSize(int maxObjectSize) {
			if (maxObjectSize <= 0) {
				throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
			}
			this.maxObjectSize = maxObjectSize;
		}

		@Override
		public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
			byte[] data = serializer.serialize((Tuple) message);
			IoBuffer buf = IoBuffer.allocate(data.length + 4);
			buf.putInt(data.length);
			buf.put(data);

			int objectSize = buf.position() - 4;
			if (objectSize > maxObjectSize) {
				throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + maxObjectSize + ')');
			}
			buf.flip();
			out.write(buf);
		}
	}

	public static class TupleDecoder extends CumulativeProtocolDecoder {

		private final Serializer serializer;
		private int maxObjectSize = 8 * 1024 * 1024;

		public TupleDecoder(Serializer serializer) {
			this.serializer = serializer;
		}

		public int getMaxObjectSize() {
			return maxObjectSize;
		}

		public void setMaxObjectSize(int maxObjectSize) {
			if (maxObjectSize <= 0) {
				throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
			}
			this.maxObjectSize = maxObjectSize;
		}

		@Override
		protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
			if (!in.prefixedDataAvailable(4, maxObjectSize)) {
				return false;
			}
			int length = in.getInt();
			if (length <= 4) {
				throw new BufferDataException("Object length should be greater than 4: " + length);
			}
			byte[] data = new byte[length];
			in.get(data);
			Tuple tuple = serializer.deserialize(data);
			out.write(tuple);
			return true;
		}

	}

}
