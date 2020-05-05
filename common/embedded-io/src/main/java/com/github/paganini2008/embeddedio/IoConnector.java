package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 * IoConnector
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface IoConnector {

	void setWriterBatchSize(int batchSize);

	void setWriterBufferSize(int bufferSize);

	void setTransformer(Transformer transformer);

	void addHandler(ChannelHandler channelHandler);

	void connect(SocketAddress remoteAddress) throws IOException;

	void write(Object object);

	void flush();

	boolean isActive();

	void close();

}