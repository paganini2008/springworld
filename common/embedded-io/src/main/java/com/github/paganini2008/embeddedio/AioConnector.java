package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * AioConnector
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class AioConnector implements IoConnector {

	private static final Log logger = LogFactory.getLog(AioConnector.class);
	private final ChannelEventPublisher channelEventPublisher;
	private Transformer transformer = new SerializationTransformer();
	private int writerBatchSize = 1;
	private int writerBufferSize = 1024;
	private int autoFlushInterval = 0;
	private final Observable observable = Observable.unrepeatable();

	public AioConnector() {
		this(Executors.newCachedThreadPool());
	}

	public AioConnector(Executor executor) {
		this.channelEventPublisher = new DefaultChannelEventPublisher(executor);
		initialize();
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

	protected void initialize() {
		addHandler(new ChannelFutureHandler());
	}

	@Override
	public Channel connect(SocketAddress remoteAddress, ChannelPromise<Channel> promise) throws IOException {
		AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
		socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, writerBufferSize);
		socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		try {
			socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (RuntimeException e) {
			logger.warn(e.getMessage());
		}
		Channel channel = new AioChannel(socketChannel, channelEventPublisher, transformer, writerBatchSize, autoFlushInterval);
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
		if (promise != null) {
			observable.addObserver((ob, arg) -> {
				if (arg instanceof Throwable) {
					promise.onFailure((Throwable) arg);
				} else {
					promise.onSuccess((Channel) arg);
				}
			});
		}
		return channel;
	}

	@Override
	public void close() {
		channelEventPublisher.destroy();
	}

	private class ChannelFutureHandler implements ChannelHandler {

		@Override
		public void fireChannelActive(Channel channel) throws IOException {
			observable.notifyObservers(channel);
		}

		@Override
		public void fireChannelFatal(Channel channel, Throwable e) {
			observable.notifyObservers(e);
		}

	}

}
