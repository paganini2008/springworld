package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * NioConnector
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class NioConnector extends NioReactor implements IoConnector {

	public NioConnector() {
		this(Executors.newCachedThreadPool());
	}

	public NioConnector(Executor executor) {
		super(true);
		this.reader = new NioReader();
		this.channelEventPublisher = new DefaultChannelEventPublisher(executor);
		initialize();
	}

	private final NioReader reader;
	private final ChannelEventPublisher channelEventPublisher;
	private Transformer transformer = new SerializationTransformer();
	private int writerBatchSize = 1;
	private int writerBufferSize = 1024;
	private int autoFlushInterval = 0;
	private final Observable observable = Observable.unrepeatable();

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

	protected void initialize() {
		addHandler(new ChannelFutureHandler());
	}

	public Channel connect(SocketAddress remoteAddress, ChannelPromise<Channel> promise) throws IOException {
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
		if (promise != null) {
			observable.addObserver(remoteAddress.toString(), (ob, arg) -> {
				if (arg instanceof Throwable) {
					promise.onFailure((Throwable) arg);
				} else {
					promise.onSuccess((Channel) arg);
				}
			});
		}
		Channel channel = new NioChannel(socketChannel, channelEventPublisher, transformer, writerBatchSize, autoFlushInterval);
		register(socketChannel, SelectionKey.OP_CONNECT, channel);
		return channel;
	}

	public void close() {
		channelEventPublisher.destroy();
		reader.destroy();
		destroy();
	}

	@Override
	protected boolean isSelectable(SelectionKey selectionKey) {
		return selectionKey.isConnectable();
	}

	@Override
	protected void process(SelectionKey selectionKey) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		final Channel channel = (Channel) selectionKey.attachment();
		boolean connected;
		if (socketChannel.isConnectionPending()) {
			try {
				while (!socketChannel.finishConnect()) {
					;
				}
				connected = true;
			} catch (IOException e) {
				connected = false;
				channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.FATAL, null, e));
			}
		} else {
			connected = socketChannel.isConnected();
		}
		if (connected) {
			reader.register(socketChannel, SelectionKey.OP_READ, channel);
			channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.ACTIVE));
		}
	}

	private class ChannelFutureHandler implements ChannelHandler {

		@Override
		public void fireChannelActive(Channel channel) throws IOException {
			SocketAddress remoteAddress = channel.getRemoteAddr();
			observable.notifyObservers(remoteAddress.toString(), channel);
		}

		@Override
		public void fireChannelFatal(Channel channel, Throwable e) {
			observable.notifyObservers(e);
		}

	}

}
