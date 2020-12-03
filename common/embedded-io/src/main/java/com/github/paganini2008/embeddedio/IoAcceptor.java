package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 * IoAcceptor
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface IoAcceptor {

	void setBacklog(int backlog);

	void setLocalAddress(SocketAddress localAddress);

	void setTransformer(Transformer transformer);

	void setReaderBufferSize(int readerBufferSize);

	void addHandler(ChannelHandler channelHandler);

	void start() throws IOException;

	void stop();

	boolean isOpened();

}