package com.github.paganini2008.transport.netty;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.serializer.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * 
 * NettyEncoderDecoders
 * 
 * @author Fred Feng
 * @version 1.0
 */
public abstract class NettyEncoderDecoders {

	public static class JsonEncoder extends MessageToMessageEncoder<CharSequence> {

		private final Charset charset;

		public JsonEncoder(Charset charset) {
			this.charset = charset;
		}

		@Override
		protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
			ByteBuf buf = Unpooled.buffer(msg.length() + 4);
			buf.writeInt(msg.length());
			buf.writeCharSequence(msg, charset);
			out.add(buf);
		}

	}

	public static class JsonToTupleDecoder extends MessageToMessageDecoder<ByteBuf> {

		private final Charset charset;
		private final ObjectMapper objectMapper = new ObjectMapper();

		public JsonToTupleDecoder(Charset charset) {
			this.charset = charset;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			if (in.readableBytes() < 4) {
				return;
			}
			in.markReaderIndex();
			int dataLength = in.readInt();
			if (dataLength < 0) {
				ctx.close();
			}
			if (in.readableBytes() < dataLength) {
				in.resetReaderIndex();
				return;
			}
			CharSequence value = in.readCharSequence(dataLength, charset);
			Map<String, Object> map = objectMapper.readValue(value.toString(), HashMap.class);
			out.add(Tuple.wrap(map));
		}

	}

	public static class TupleEncoder extends MessageToByteEncoder<Tuple> {

		private final Serializer serializer;

		public TupleEncoder(Serializer serializer) {
			this.serializer = serializer;
		}

		@Override
		protected void encode(ChannelHandlerContext ctx, Tuple tuple, ByteBuf out) throws Exception {
			byte[] data = serializer.serialize(tuple);
			out.writeInt(data.length);
			out.writeBytes(data);
		}

	}

	public static class TupleDecoder extends ByteToMessageDecoder {

		private final Serializer serializer;

		public TupleDecoder(Serializer serializer) {
			this.serializer = serializer;
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			if (in.readableBytes() < 4) {
				return;
			}
			in.markReaderIndex();
			int dataLength = in.readInt();
			if (dataLength < 0) {
				ctx.close();
			}

			if (in.readableBytes() < dataLength) {
				in.resetReaderIndex();
				return;
			}

			byte[] body = new byte[dataLength];
			in.readBytes(body);
			Tuple tuple = serializer.deserialize(body);
			out.add(tuple);
		}

	}

}
