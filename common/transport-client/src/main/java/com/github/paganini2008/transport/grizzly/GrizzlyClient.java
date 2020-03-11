package com.github.paganini2008.transport.grizzly;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;

import com.github.paganini2008.transport.ConnectionWatcher;
import com.github.paganini2008.transport.HandshakeCallback;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.TransportClientException;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * GrizzlyClient
 *
 * @author Fred Feng
 * @version 1.0
 */
public class GrizzlyClient implements NioClient {

	private final AtomicBoolean opened = new AtomicBoolean(false);
	private final GrizzlyChannelContext channelContext = new GrizzlyChannelContext();
	private int idleTimeout = 30;
	private int threadCount = -1;
	private TCPNIOTransport transport;
	private DelayedExecutor delayedExecutor;
	private MessageCodecFactory messageCodecFactory;

	@Override
	public void open() {
		FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
		filterChainBuilder.add(new TransportFilter());
		delayedExecutor = IdleTimeoutFilter.createDefaultIdleDelayedExecutor(5, TimeUnit.SECONDS);
		delayedExecutor.start();
		IdleTimeoutFilter timeoutFilter = new IdleTimeoutFilter(delayedExecutor, idleTimeout, TimeUnit.SECONDS, IdleTimeoutHandlers.PING);
		filterChainBuilder.add(timeoutFilter);
		if (messageCodecFactory == null) {
			messageCodecFactory = new GrizzlyTupleCodecFactory();
		}
		filterChainBuilder.add(new TupleFilter(messageCodecFactory));
		filterChainBuilder.add(channelContext);
		TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
		ThreadPoolConfig tpConfig = ThreadPoolConfig.defaultConfig();
		int nThreads = threadCount > 0 ? threadCount : Runtime.getRuntime().availableProcessors() * 2;
		tpConfig.setPoolName("GrizzlyClientHandler").setQueueLimit(-1).setCorePoolSize(nThreads).setMaxPoolSize(nThreads)
				.setKeepAliveTime(60L, TimeUnit.SECONDS);
		builder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
		builder.setWorkerThreadPoolConfig(tpConfig);
		builder.setKeepAlive(true).setTcpNoDelay(true).setConnectionTimeout(60000).setWriteBufferSize(1024 * 1024);
		transport = builder.build();
		transport.setProcessor(filterChainBuilder.build());
		try {
			transport.start();
		} catch (IOException e) {
			throw new TransportClientException(e.getMessage(), e);
		}
		opened.set(true);
	}

	@Override
	public void close() {
		try {
			channelContext.getChannels().forEach(connection -> {
				connection.close();
			});
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
		try {
			delayedExecutor.destroy();
			transport.shutdown(60, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
		opened.set(false);
	}

	@Override
	public boolean isOpened() {
		return opened.get();
	}

	@Override
	public void connect(SocketAddress remoteAddress, HandshakeCallback handshakeCallback) {
		if (isConnected(remoteAddress)) {
			return;
		}
		try {
			transport.connect(remoteAddress, new DefaultCompletionHandler(handshakeCallback));
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	class DefaultCompletionHandler implements CompletionHandler<Connection> {

		private final HandshakeCallback handshakeCallback;

		DefaultCompletionHandler(HandshakeCallback handshakeCallback) {
			this.handshakeCallback = handshakeCallback;
		}

		public void cancelled() {
			throw new TransportClientException("Connection is cancelled.");
		}

		public void failed(Throwable e) {
			throw new TransportClientException(e.getMessage(), e);
		}

		public void updated(Connection connection) {
		}

		public void completed(Connection connection) {
			if (connection.isOpen()) {
				SocketAddress remoteAddress = (SocketAddress) connection.getPeerAddress();
				ConnectionWatcher connectionWatcher = channelContext.getConnectionWatcher();
				if (connectionWatcher != null) {
					connectionWatcher.watch(remoteAddress, handshakeCallback);
				}
				if (handshakeCallback != null) {
					handshakeCallback.operationComplete(remoteAddress);
				}
			}
		}

	}

	@Override
	public boolean isConnected(SocketAddress remoteAddress) {
		Connection<?> connection = channelContext.getChannel(remoteAddress);
		return connection != null && connection.isOpen();
	}

	public void setMessageCodecFactory(MessageCodecFactory messageCodecFactory) {
		this.messageCodecFactory = messageCodecFactory;
	}

	@Override
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Override
	public void watchConnection(int interval, TimeUnit timeUnit) {
		this.channelContext.setConnectionWatcher(new ConnectionWatcher(interval, timeUnit, this));
	}

	@Override
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	@Override
	public void send(Object data) {
		channelContext.getChannels().forEach(connection -> {
			doSend(connection, data);
		});
	}

	@Override
	public void send(Object data, Partitioner partitioner) {
		Connection<?> connection = channelContext.selectChannel(data, partitioner);
		if (connection != null) {
			doSend(connection, data);
		}
	}

	protected void doSend(Connection<?> connection, Object data) {
		try {
			if (data instanceof CharSequence) {
				connection.write(Tuple.byString(((CharSequence) data).toString()));
			} else if (data instanceof Tuple) {
				connection.write(data);
			}
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

}
