package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.paganini2008.devtools.multithreads.AtomicIntegerSequence;

/**
 * 
 * NioAcceptor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class NioAcceptor extends NioReactor implements IoAcceptor {

	private static final int processorCount = Runtime.getRuntime().availableProcessors();
	private ServerSocketChannel serverChannel;
	private int backlog = 128;
	private Transformer transformer = new SerializationTransformer();
	private SocketAddress localAddress = new InetSocketAddress(8090);
	private int readerBufferSize = 2 * 1024;
	private final AtomicIntegerSequence readerIndex;
	private final ChannelEventPublisher channelEventPublisher;
	private final ConcurrentMap<Integer, NioReader> readers = new ConcurrentHashMap<Integer, NioReader>();

	public NioAcceptor() {
		this(Executors.newFixedThreadPool(processorCount * 2));
	}

	public NioAcceptor(Executor executor) {
		super(false);
		this.channelEventPublisher = new DefaultChannelEventPublisher(executor);
		this.readerIndex = new AtomicIntegerSequence(1, processorCount);
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public SocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(SocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public int getReaderBufferSize() {
		return readerBufferSize;
	}

	public void setReaderBufferSize(int readerBufferSize) {
		this.readerBufferSize = readerBufferSize;
	}

	public void addHandler(ChannelHandler channelHandler) {
		this.channelEventPublisher.subscribeChannelEvent(channelHandler);
	}

	public void start() throws IOException {
		serverChannel = ServerSocketChannel.open();
		final ServerSocket socket = serverChannel.socket();
		if (readerBufferSize > 0) {
			socket.setReceiveBufferSize(readerBufferSize);
		}
		socket.setReuseAddress(true);
		socket.bind(localAddress, backlog);
		serverChannel.configureBlocking(false);
		register(serverChannel, SelectionKey.OP_ACCEPT, null);
		logger.info("Server bind at " + localAddress);
	}

	public void stop() {
		channelEventPublisher.destroy();
		for (NioReactor reactor : readers.values()) {
			reactor.destroy();
		}
		if (serverChannel != null) {
			try {
				serverChannel.close();
			} catch (IOException ignored) {
			}
		}
		destroy();
		logger.info("Server has stopped.");
	}

	@Override
	protected boolean isSelectable(SelectionKey selectionKey) {
		return selectionKey.isAcceptable();
	}

	@Override
	protected void process(SelectionKey selectionKey) throws IOException {
		SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
		socketChannel.configureBlocking(false);
		Channel channel = new NioChannel(socketChannel, channelEventPublisher, transformer, 1, 0);
		NioReader nextReactor = readers.getOrDefault(readerIndex.getAndIncrement(), new NioReader());
		nextReactor.register(socketChannel, SelectionKey.OP_READ, channel);
		channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, ChannelEvent.EventType.ACTIVE));
	}

}
