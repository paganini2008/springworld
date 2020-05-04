package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * NioConnector
 *
 * @author Fred Feng
 * @since 1.0
 */
public class NioConnector extends NioReactor implements IoConnector {

	public NioConnector() {
		this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
	}

	public NioConnector(Executor executor) {
		this.reader = new NioReader();
		this.channelEventPublisher = new DefaultChannelEventPublisher(executor);
	}

	private final NioReader reader;
	private final ChannelEventPublisher channelEventPublisher;
	private Channel channel;
	private Transformer transformer = new SerializationTransformer();
	private int writerBatchSize = 1;
	private int writerBufferSize = 1024;
	private int autoFlushInterval = 0;

	public int getWriterBatchSize() {
		return writerBatchSize;
	}

	public void setWriterBatchSize(int writerBatchSize) {
		this.writerBatchSize = writerBatchSize;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public int getWriterBufferSize() {
		return writerBufferSize;
	}

	public void setWriterBufferSize(int writerBufferSize) {
		this.writerBufferSize = writerBufferSize;
	}

	public int getAutoFlushInterval() {
		return autoFlushInterval;
	}

	public void setAutoFlushInterval(int autoFlushInterval) {
		this.autoFlushInterval = autoFlushInterval;
	}

	public void addHandler(ChannelHandler channelHandler) {
		this.channelEventPublisher.subscribeChannelEvent(channelHandler);
	}

	public void connect(SocketAddress remoteAddress) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		final Socket socket = socketChannel.socket();
		socket.setKeepAlive(true);
		socket.setReuseAddress(true);
		socket.setTcpNoDelay(true);
		if (writerBufferSize > 0) {
			socket.setSendBufferSize(writerBufferSize);
		}
		socketChannel.configureBlocking(false);
		socketChannel.connect(remoteAddress);
		channel = new NioChannel(channelEventPublisher, socketChannel, transformer, autoFlushInterval);
		register(socketChannel, SelectionKey.OP_CONNECT, channel);
	}

	public void write(Object object) {
		channel.write(object, writerBatchSize);
	}

	public void flush() {
		channel.flush();
	}

	public boolean isActive() {
		return channel != null && channel.isActive();
	}

	public void close() {
		reader.destroy();
		channelEventPublisher.destroy();
		if (channel != null) {
			channel.close();
		}
		destroy();
	}

	@Override
	protected boolean isSelectable(SelectionKey selectionKey) {
		return selectionKey.isConnectable();
	}

	@Override
	protected void process(SelectionKey selectionKey) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		boolean connected = true;
		if (socketChannel.isConnectionPending()) {
			try {
				while (!socketChannel.finishConnect()) {
					;
				}
			} catch (IOException e) {
				connected = false;
				channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.FATAL, null, e));
			}
		}
		if (connected) {
			reader.register(socketChannel, SelectionKey.OP_READ, channel);
			channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.ACTIVE));
		}
	}

}
