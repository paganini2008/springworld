package com.github.paganini2008.xtransport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.devtools.net.UrlUtils;

/**
 * 
 * TcpTransportClient
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class TcpTransportClient implements Executable, TransportClient {

	private static final Log logger = LogFactory.getLog(TcpTransportClient.class);

	public TcpTransportClient(String brokerUrl, NioClient nioClient, Partitioner partitioner, int startupDelay) {
		this.brokerUrl = brokerUrl;
		this.nioClient = nioClient;
		this.partitioner = partitioner;
		this.startupDelay = startupDelay;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String brokerUrl;
	private final NioClient nioClient;
	private final Partitioner partitioner;
	private final int startupDelay;
	private volatile boolean started;

	@Override
	public void send(CharSequence json) {
		send(Tuple.byString(json.toString()));
	}

	@Override
	public void send(Tuple tuple) {
		if (started && nioClient.isOpened()) {
			nioClient.send(tuple, partitioner);
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public void start() {
		if (started) {
			return;
		}
		started = true;
		if (startupDelay > 0) {
			ThreadUtils.schedule(() -> {
				doStart();
			}, startupDelay, TimeUnit.SECONDS);
		} else {
			doStart();
		}

	}

	private void doStart() {
		nioClient.open();
		doConnect();
		ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.MINUTES);
		logger.info(this + "\tStart TcpTransportClient ok.");
	}

	@Override
	public void close() {
		if (!started) {
			throw new IllegalStateException("TcpTransportClient is not started now.");
		}
		started = false;
		nioClient.close();
		logger.info("Close TcpTransportClient ok.");
	}

	private void doConnect() {
		String[] channels = getChannels();
		for (String channel : channels) {
			String[] args = channel.split(":", 2);
			try {
				nioClient.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])), location -> {
					logger.info("TransportClient connect to: " + location);
				});
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean execute() {
		doConnect();
		return started && nioClient.isOpened();
	}

	@Override
	public String[] getChannels() {
		String content;
		try {
			content = UrlUtils.toString(brokerUrl, "utf-8");
		} catch (IOException ignored) {
			return new String[0];
		}
		try {
			return objectMapper.readValue(content, String[].class);
		} catch (IOException e) {
			throw new TransportClientException("Invalid format brokerUrl: " + content, e);
		}
	}

}
