package com.github.paganini2008.springdessert.transport.transport;

import static com.github.paganini2008.springdessert.transport.Constants.PORT_RANGE_END;
import static com.github.paganini2008.springdessert.transport.Constants.PORT_RANGE_START;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.transport.ChannelEvent;
import com.github.paganini2008.transport.ChannelEvent.EventType;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.NodeFinder;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MinaServer
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class MinaServer implements NioServer {

	static {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());
	}

	private final AtomicBoolean started = new AtomicBoolean(false);
	private NioSocketAcceptor ioAcceptor;
	private InetSocketAddress localAddress;

	@Autowired
	private MinaServerHandler serverHandler;

	@Autowired
	private ProtocolCodecFactory codecFactory;

	@Autowired(required = false)
	private ChannelEventListener<IoSession> channelEventListener;

	@Value("${spring.application.transport.nioserver.threads:-1}")
	private int threadCount;

	@Value("${spring.application.transport.nioserver.hostName:}")
	private String hostName;

	@Value("${spring.application.transport.nioserver.idleTimeout:60}")
	private int idleTimeout;

	@Value("${spring.application.transport.nioserver.keepalive.response:true}")
	private boolean keepaliveResposne;

	@Autowired
	private NodeFinder nodeFinder;

	@Override
	public int start() {
		if (isStarted()) {
			throw new IllegalStateException("MinaServer has been started.");
		}
		final int nThreads = threadCount > 0 ? threadCount : Runtime.getRuntime().availableProcessors() * 2;
		ioAcceptor = new NioSocketAcceptor(nThreads);
		ioAcceptor.setBacklog(128);
		SocketSessionConfig sessionConfig = ioAcceptor.getSessionConfig();
		sessionConfig.setKeepAlive(true);
		sessionConfig.setReuseAddress(true);
		sessionConfig.setReadBufferSize(2 * 1024 * 1024);
		sessionConfig.setReceiveBufferSize(2 * 1024 * 1024);
		sessionConfig.setSoLinger(0);
		ioAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));

		KeepAliveFilter heartBeat = new KeepAliveFilter(new ServerKeepAliveMessageFactory(), IdleStatus.READER_IDLE);
		heartBeat.setForwardEvent(false);
		heartBeat.setRequestTimeout(idleTimeout);
		heartBeat.setRequestTimeoutHandler(KeepAliveRequestTimeoutHandler.LOG);
		ioAcceptor.getFilterChain().addLast("heartbeat", heartBeat);

		ioAcceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(nThreads));
		ioAcceptor.setHandler(serverHandler);
		int port = NetUtils.getRandomPort(PORT_RANGE_START, PORT_RANGE_END);
		try {
			localAddress = StringUtils.isNotBlank(hostName) ? new InetSocketAddress(hostName, port) : new InetSocketAddress(port);
			ioAcceptor.bind(localAddress);
			String location = localAddress.getHostName() + ":" + localAddress.getPort();
			nodeFinder.registerNode(location);
			started.set(true);
			log.info("MinaServer is started on: " + localAddress);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return port;
	}

	@Override
	public void stop() {
		if (ioAcceptor == null || !isStarted()) {
			return;
		}
		try {
			ioAcceptor.unbind(localAddress);
			ExecutorFilter executorFilter = (ExecutorFilter) ioAcceptor.getFilterChain().get("threadPool");
			if (executorFilter != null) {
				executorFilter.destroy();
			}
			ioAcceptor.getFilterChain().clear();
			ioAcceptor.dispose();
			ioAcceptor = null;

			started.set(false);
			log.info("MinaServer is closed.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean isStarted() {
		return started.get();
	}

	class ServerKeepAliveMessageFactory implements KeepAliveMessageFactory {

		public boolean isRequest(IoSession session, Object message) {
			return (message instanceof Tuple) && ((Tuple) message).isPing();
		}

		public boolean isResponse(IoSession session, Object message) {
			return false;
		}

		public Object getRequest(IoSession session) {
			return null;
		}

		public Object getResponse(IoSession session, Object request) {
			if (channelEventListener != null) {
				channelEventListener.fireChannelEvent(new ChannelEvent<IoSession>(session, EventType.PING, null));
			}
			return keepaliveResposne ? Tuple.PONG : null;
		}
	}

}
