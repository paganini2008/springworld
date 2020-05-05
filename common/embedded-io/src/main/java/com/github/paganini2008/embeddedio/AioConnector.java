package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * AioConnector
 *
 * @author Fred Feng
 * @since 1.0
 */
public class AioConnector implements IoConnector {

	private static final Log logger = LogFactory.getLog(AioConnector.class);
	private final ChannelEventPublisher channelEventPublisher;
	private Channel channel;
	private Transformer transformer = new SerializationTransformer();
	private int writerBatchSize = 1;
	private int writerBufferSize = 1024;
	private int autoFlushInterval = 0;

	public AioConnector() {
		this(Executors.newCachedThreadPool());
	}

	public AioConnector(Executor executor) {
		this.channelEventPublisher = new DefaultChannelEventPublisher(executor);
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public int getWriterBatchSize() {
		return writerBatchSize;
	}

	public void setWriterBatchSize(int writerBatchSize) {
		this.writerBatchSize = writerBatchSize;
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

	@Override
	public void addHandler(ChannelHandler channelHandler) {
		this.channelEventPublisher.subscribeChannelEvent(channelHandler);
	}

	@Override
	public void connect(SocketAddress remoteAddress) throws IOException {
		AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
		socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, writerBufferSize);
		socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		try {
			socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (RuntimeException e) {
			logger.warn(e.getMessage());
		}
		channel = new AioChannel(channelEventPublisher, socketChannel, transformer, autoFlushInterval);
		socketChannel.connect(remoteAddress, null, new CompletionHandler<Void, Object>() {

			@Override
			public void completed(Void result, Object attr) {
				channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.ACTIVE));
				channel.read();
			}

			@Override
			public void failed(Throwable e, Object attr) {
				channelEventPublisher.publishChannelEvent(new ChannelEvent(channel, EventType.FATAL, null, e));
			}
		});

	}

	@Override
	public void write(Object object) {
		channel.write(object, writerBatchSize);
	}

	@Override
	public void flush() {
		channel.flush();
	}

	@Override
	public boolean isActive() {
		return channel.isActive();
	}

	@Override
	public void close() {
		channelEventPublisher.destroy();
		if (channel != null) {
			channel.close();
		}
	}

}
