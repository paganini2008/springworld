package com.github.paganini2008.springworld.transport.transport;

import static com.github.paganini2008.springworld.transport.Constants.APPLICATION_KEY;
import static com.github.paganini2008.springworld.transport.Constants.PORT_RANGE_END;
import static com.github.paganini2008.springworld.transport.Constants.PORT_RANGE_START;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.transport.grizzly.IdleTimeoutFilter;
import com.github.paganini2008.transport.grizzly.IdleTimeoutPolicies;
import com.github.paganini2008.transport.grizzly.TupleCodecFactory;
import com.github.paganini2008.transport.grizzly.TupleFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * GrizzlyServer
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class GrizzlyServer implements NioServer {

	private final AtomicBoolean started = new AtomicBoolean(false);
	private TCPNIOTransport transport;
	private DelayedExecutor delayedExecutor;
	private InetSocketAddress localAddress;

	@Value("${spring.transport.nioserver.threads:-1}")
	private int threadCount;

	@Value("${spring.transport.nioserver.hostName:}")
	private String hostName;

	@Value("${spring.transport.nioserver.idleTimeout:60}")
	private int idleTimeout;

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private GrizzlyServerHandler serverHandler;

	@Autowired
	private TupleCodecFactory codecFactory;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public int start() {
		if (isStarted()) {
			throw new IllegalStateException("GrizzlyServer has been started.");
		}
		FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
		filterChainBuilder.add(new TransportFilter());
		delayedExecutor = IdleTimeoutFilter.createDefaultIdleDelayedExecutor(5, TimeUnit.SECONDS);
		delayedExecutor.start();
		IdleTimeoutFilter timeoutFilter = new IdleTimeoutFilter(delayedExecutor, idleTimeout, TimeUnit.SECONDS,
				IdleTimeoutPolicies.READER_IDLE_LOG);
		filterChainBuilder.add(timeoutFilter);
		filterChainBuilder.add(new TupleFilter(codecFactory));
		filterChainBuilder.add(serverHandler);
		TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
		final int nThreads = threadCount > 0 ? threadCount : Runtime.getRuntime().availableProcessors() * 2;
		ThreadPoolConfig tpConfig = ThreadPoolConfig.defaultConfig();
		tpConfig.setPoolName("GrizzlyServerHandler").setQueueLimit(-1).setCorePoolSize(nThreads).setMaxPoolSize(nThreads)
				.setKeepAliveTime(60L, TimeUnit.SECONDS);
		builder.setWorkerThreadPoolConfig(tpConfig);
		builder.setKeepAlive(true).setReuseAddress(true).setReadBufferSize(2 * 1024 * 1024);
		builder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
		builder.setServerConnectionBackLog(128);
		transport = builder.build();
		transport.setProcessor(filterChainBuilder.build());
		int port = NetUtils.getRandomPort(PORT_RANGE_START, PORT_RANGE_END);
		try {
			localAddress = StringUtils.isNotBlank(hostName) ? new InetSocketAddress(hostName, port) : new InetSocketAddress(port);
			transport.bind(localAddress);
			transport.start();
			String location = localAddress.getHostName() + ":" + localAddress.getPort();
			String key = String.format(APPLICATION_KEY, applicationName);
			redisTemplate.opsForHash().put(key, clusterId.get(), location);
			started.set(true);
			log.info("GrizzlyServer is started on: " + localAddress);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return port;
	}

	@Override
	public void stop() {
		if (transport == null || !isStarted()) {
			return;
		}
		try {
			delayedExecutor.destroy();
			transport.shutdown(60, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		started.set(false);
		log.info("GrizzlyServer is shutdown.");
	}

	@Override
	public boolean isStarted() {
		return started.get();
	}

}
