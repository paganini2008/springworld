package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.ExecutorUtils;

/**
 * 
 * AioAcceptor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class AioAcceptor implements IoAcceptor {

	private static final int processorCount = Runtime.getRuntime().availableProcessors();
	private static final Log logger = LogFactory.getLog(AioAcceptor.class);
	private final ExecutorService bossExecutor;
	private final ChannelEventPublisher channelEventPublisher;
	private AsynchronousChannelGroup channelGroup;
	private AsynchronousServerSocketChannel channel;
	private int backlog = 128;
	private Transformer transformer = new SerializationTransformer();
	private SocketAddress localAddress = new InetSocketAddress(8090);
	private int readerBufferSize = 2 * 1024;
	private final AtomicBoolean opened = new AtomicBoolean();
	private final Map<AsynchronousSocketChannel, Channel> channelHolder = new ConcurrentHashMap<AsynchronousSocketChannel, Channel>();

	public AioAcceptor() {
		this(Executors.newFixedThreadPool(processorCount * 2));
	}

	public AioAcceptor(ExecutorService executor) {
		this(executor, executor);
	}

	public AioAcceptor(ExecutorService bossExecutor, ExecutorService workerExecutor) {
		this.bossExecutor = bossExecutor;
		this.channelEventPublisher = new DefaultChannelEventPublisher(workerExecutor);
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

	private class AcceptorHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

		@Override
		public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
			if (!isOpened()) {
				return;
			}
			channel.accept(null, this);
			Channel channelWrapper = channelHolder.getOrDefault(socketChannel,
					new AioChannel(socketChannel, channelEventPublisher, transformer, 1, 0));
			channelEventPublisher.publishChannelEvent(new ChannelEvent(channelWrapper, ChannelEvent.EventType.ACTIVE));
			channelWrapper.read();
		}

		@Override
		public void failed(Throwable e, Object attachment) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void start() throws IOException {
		channelGroup = AsynchronousChannelGroup.withThreadPool(bossExecutor);
		channel = AsynchronousServerSocketChannel.open(channelGroup);
		channel.setOption(StandardSocketOptions.SO_RCVBUF, readerBufferSize);
		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		channel.bind(localAddress, backlog);
		channel.accept(null, new AcceptorHandler());
		opened.set(true);
		logger.info("Server bind at " + localAddress);
	}

	@Override
	public void stop() {
		opened.set(false);

		channelEventPublisher.destroy();
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException ignored) {
			}
		}
		if (channelGroup != null) {
			while (!channelGroup.isShutdown() || !channelGroup.isTerminated()) {
				try {
					channelGroup.awaitTermination(3, TimeUnit.SECONDS);
					channelGroup.shutdown();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					try {
						channelGroup.shutdownNow();
					} catch (IOException ignored) {
					}
				}
			}
		}
		ExecutorUtils.gracefulShutdown(bossExecutor, 60000L);
		logger.info("Server has stopped.");
	}

	@Override
	public boolean isOpened() {
		return opened.get();
	}

}
