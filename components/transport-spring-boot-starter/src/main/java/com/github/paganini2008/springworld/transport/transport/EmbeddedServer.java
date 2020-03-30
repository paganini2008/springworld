package com.github.paganini2008.springworld.transport.transport;

import static com.github.paganini2008.springworld.transport.Constants.APPLICATION_KEY;
import static com.github.paganini2008.springworld.transport.Constants.PORT_RANGE_END;
import static com.github.paganini2008.springworld.transport.Constants.PORT_RANGE_START;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.springworld.cluster.ClusterId;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedServer
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class EmbeddedServer implements NioServer {

	private final AtomicBoolean started = new AtomicBoolean(false);

	@Value("${spring.transport.nioserver.threads:-1}")
	private int threadCount;

	@Value("${spring.transport.nioserver.hostName:}")
	private String hostName;

	@Value("${spring.transport.nioserver.idleTimeout:60}")
	private int idleTimeout;

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public int start() throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 2 * 1024 * 1024);
		Selector selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		int port = NetUtils.getRandomPort(PORT_RANGE_START, PORT_RANGE_END);
		InetSocketAddress localAddress = StringUtils.isNotBlank(hostName) ? new InetSocketAddress(hostName, port)
				: new InetSocketAddress(port);
		serverChannel.bind(localAddress, 1024);
		String location = localAddress.getHostName() + ":" + localAddress.getPort();
		String key = String.format(APPLICATION_KEY, applicationName);
		redisTemplate.opsForHash().put(key, clusterId.get(), location);
		started.set(true);
		log.info("EmbeddedServer is started on: " + localAddress);
		return port;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		return started.get();
	}

}
