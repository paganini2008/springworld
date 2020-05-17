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

	void setAutoFlushInterval(int autoFlushInterval);

	void setTransformer(Transformer transformer);

	void addHandler(ChannelHandler channelHandler);

	Channel connect(SocketAddress remoteAddress, ChannelPromise<Channel> promise) throws IOException;

	void close();

}