package com.github.paganini2008.transport.embeddedio;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;
import com.github.paganini2008.embeddedio.AioConnector;
import com.github.paganini2008.embeddedio.Channel;
import com.github.paganini2008.embeddedio.ChannelHandler;
import com.github.paganini2008.embeddedio.IdleChannelHandler;
import com.github.paganini2008.embeddedio.IdleTimeoutListener;
import com.github.paganini2008.embeddedio.IoConnector;
import com.github.paganini2008.embeddedio.NioConnector;
import com.github.paganini2008.embeddedio.SerializationTransformer;
import com.github.paganini2008.transport.ConnectionWatcher;
import com.github.paganini2008.transport.HandshakeCallback;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.TransportClientException;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * EmbeddedClient
 *
 * @author Fred Feng
 * @since 1.0
 */
public class EmbeddedClient implements NioClient {

	private final AtomicBoolean opened = new AtomicBoolean(false);
	private final EmbeddedChannelContext channelContext = new EmbeddedChannelContext();
	private IoConnector connector;
	private int idleTimeout = 30;
	private int threadCount = -1;

	@Override
	public void open() {
		final int nThreads = threadCount > 0 ? threadCount : Runtime.getRuntime().availableProcessors() * 2;
		Executor threadPool = Executors.newFixedThreadPool(nThreads, new PooledThreadFactory("transport-embedded-client-threads-"));
		connector = useAio ? new AioConnector(threadPool) : new NioConnector(threadPool);
		connector.setWriterBatchSize(100);
		connector.setWriterBufferSize(1024 * 1024);
		connector.setAutoFlushInterval(3);
		if (idleTimeout > 0) {
			connector.addHandler(IdleChannelHandler.writerIdle(idleTimeout, 60, TimeUnit.SECONDS, new PingIdleTimeoutListener()));
		}
		connector.addHandler(channelContext);
		opened.set(true);
	}

	private boolean useAio = false;

	public void setUseAio(boolean useAio) {
		this.useAio = useAio;
	}

	@Override
	public void close() {
		try {
			channelContext.getChannels().forEach(channel -> {
				channel.close();
			});
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
		if (connector != null) {
			connector.close();
		}
		opened.set(false);
	}

	@Override
	public boolean isOpened() {
		return opened.get();
	}

	@Override
	public void connect(final SocketAddress remoteAddress, final HandshakeCallback handshakeCallback) {
		if (isConnected(remoteAddress)) {
			return;
		}
		connector.addHandler(new ChannelHandler() {

			@Override
			public void fireChannelActive(Channel channel) throws IOException {
				ConnectionWatcher connectionWatcher = channelContext.getConnectionWatcher();
				if (connectionWatcher != null) {
					connectionWatcher.watch(channel.getRemoteAddr(), handshakeCallback);
				}
				handshakeCallback.operationComplete(channel.getRemoteAddr());
			}

		});
		try {
			connector.connect(remoteAddress);
		} catch (IOException e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isConnected(SocketAddress remoteAddress) {
		Channel channel = channelContext.getChannel(remoteAddress);
		return channel != null && channel.isActive();
	}

	@Override
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Override
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	@Override
	public void watchConnection(int checkInterval, TimeUnit timeUnit) {
		this.channelContext.setConnectionWatcher(new ConnectionWatcher(checkInterval, timeUnit, this));
	}

	public void setSerializationFactory(SerializationFactory serializationFactory) {
		SerializationTransformer transformer = new SerializationTransformer();
		transformer.setSerialization(serializationFactory.getEncoder(), serializationFactory.getDecoder());
		this.connector.setTransformer(transformer);
	}

	@Override
	public void send(Object data) {
		channelContext.getChannels().forEach(connection -> {
			doSend(connection, data);
		});
	}

	@Override
	public void send(Object data, Partitioner partitioner) {
		Channel channel = channelContext.selectChannel(data, partitioner);
		if (channel != null) {
			doSend(channel, data);
		}
	}

	protected void doSend(Channel channel, Object data) {
		try {
			if (data instanceof CharSequence) {
				channel.writeAndFlush(Tuple.byString(((CharSequence) data).toString()));
			} else if (data instanceof Tuple) {
				channel.writeAndFlush(data);
			}
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * PingIdleTimeoutListener
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	private static class PingIdleTimeoutListener implements IdleTimeoutListener {

		@Override
		public void handleIdleTimeout(Channel channel, long timeout) {
			channel.writeAndFlush(Tuple.PING);
		}
	}

}
