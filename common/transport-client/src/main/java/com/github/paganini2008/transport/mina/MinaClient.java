package com.github.paganini2008.transport.mina;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.github.paganini2008.transport.ChannelEvent;
import com.github.paganini2008.transport.ChannelEvent.EventType;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.ConnectionWatcher;
import com.github.paganini2008.transport.HandshakeCallback;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.TransportClientException;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * MinaClient
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public class MinaClient implements NioClient {

	private static final String PING = "PING";
	private static final String PONG = "PONG";

	static {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());
	}

	private final MinaChannelContext channelContext = new MinaChannelContext();
	private final AtomicBoolean opened = new AtomicBoolean(false);
	private ProtocolCodecFactory protocolCodecFactory;
	private NioSocketConnector connector;
	private int idleTimeout = 30;
	private int threadCount = Runtime.getRuntime().availableProcessors() * 2;

	@Override
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public void setProtocolCodecFactory(ProtocolCodecFactory protocolCodecFactory) {
		this.protocolCodecFactory = protocolCodecFactory;
	}

	@Override
	public void watchConnection(int interval, TimeUnit timeUnit) {
		this.channelContext.setConnectionWatcher(new ConnectionWatcher(interval, timeUnit, this));
	}

	@Override
	public void setThreadCount(int nThreads) {
		this.threadCount = nThreads;
	}

	public void setChannelEventListener(ChannelEventListener<IoSession> channelEventListener) {
		this.channelContext.setChannelEventListener(channelEventListener);
	}

	@Override
	public void open() {
		connector = new NioSocketConnector(threadCount);
		connector.setConnectTimeoutMillis(60000);
		SocketSessionConfig sessionConfig = connector.getSessionConfig();
		sessionConfig.setKeepAlive(true);
		sessionConfig.setTcpNoDelay(true);
		sessionConfig.setSendBufferSize(1024 * 1024);
		if (protocolCodecFactory == null) {
			protocolCodecFactory = new MinaTupleCodecFactory();
		}

		KeepAliveFilter heartBeat = new KeepAliveFilter(new ClientKeepAliveMessageFactory(), IdleStatus.WRITER_IDLE);
		heartBeat.setForwardEvent(false);
		heartBeat.setRequestTimeout(idleTimeout);
		heartBeat.setRequestTimeoutHandler(KeepAliveRequestTimeoutHandler.LOG);
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(protocolCodecFactory));
		connector.getFilterChain().addLast("heartbeat", heartBeat);
		connector.setHandler(channelContext);

		opened.set(true);
	}

	@Override
	public void close() {
		try {
			channelContext.getChannels().forEach(ioSession -> {
				ioSession.closeNow();
			});
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
		try {
			if (connector != null) {
				connector.getFilterChain().clear();
				connector.dispose();
				connector = null;
			}
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
	public void connect(final SocketAddress remoteAddress, final HandshakeCallback handshakeCallback) {
		if (isConnected(remoteAddress)) {
			return;
		}
		try {
			connector.connect(remoteAddress).addListener(new IoFutureListener<IoFuture>() {
				public void operationComplete(IoFuture future) {
					ConnectionWatcher connectionWatcher = channelContext.getConnectionWatcher();
					if (connectionWatcher != null) {
						connectionWatcher.watch(remoteAddress, handshakeCallback);
					}
					if (handshakeCallback != null) {
						handshakeCallback.operationComplete(remoteAddress);
					}
				}
			}).awaitUninterruptibly();
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isConnected(SocketAddress remoteAddress) {
		IoSession ioSession = channelContext.getChannel(remoteAddress);
		return ioSession != null && ioSession.isConnected();
	}

	@Override
	public void send(Object data) {
		channelContext.getChannels().forEach(ioSession -> {
			doSend(ioSession, data);
		});
	}

	@Override
	public void send(Object data, Partitioner partitioner) {
		IoSession ioSession = channelContext.selectChannel(data, partitioner);
		if (ioSession != null) {
			doSend(ioSession, data);
		}
	}

	protected void doSend(IoSession ioSession, Object data) {
		try {
			if (data instanceof CharSequence) {
				ioSession.write(Tuple.byString(((CharSequence) data).toString()));
			} else {
				ioSession.write(data);
			}
		} catch (Exception e) {
			throw new TransportClientException(e.getMessage(), e);
		}
	}

	class ClientKeepAliveMessageFactory implements KeepAliveMessageFactory {

		public boolean isRequest(IoSession session, Object message) {
			return false;
		}

		public boolean isResponse(IoSession session, Object message) {
			return (message instanceof Tuple) && PONG.equals(((Tuple) message).getField("content"));
		}

		public Object getRequest(IoSession session) {
			return Tuple.byString(PING);
		}

		public Object getResponse(IoSession session, Object request) {
			ChannelEventListener<IoSession> channelEventListener = channelContext.getChannelEventListener();
			if (channelEventListener != null) {
				channelEventListener.fireChannelEvent(new ChannelEvent<IoSession>(session, EventType.PONG, null));
			}
			return null;
		}
	}

}
