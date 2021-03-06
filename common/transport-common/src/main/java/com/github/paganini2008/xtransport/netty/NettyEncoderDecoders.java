package com.github.paganini2008.xtransport.netty;

import java.util.List;

import com.github.paganini2008.xtransport.TransportClientException;
import com.github.paganini2008.xtransport.Tuple;
import com.github.paganini2008.xtransport.serializer.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 
 * NettyEncoderDecoders
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class NettyEncoderDecoders {

	public static class TupleEncoder extends MessageToByteEncoder<Tuple> {

		private final Serializer serializer;

		public TupleEncoder(Serializer serializer) {
			this.serializer = serializer;
		}

		@Override
		protected void encode(ChannelHandlerContext ctx, Tuple input, ByteBuf out) throws Exception {
			if (input == null) {
				throw new TransportClientException("Input could not be null");
			}
			byte[] data = serializer.serialize(input);
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
			if (dataLength < 4) {
				throw new TransportClientException("Data length should be greater than 4: " + dataLength);
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
