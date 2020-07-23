package com.github.paganini2008.transport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.devtools.net.UrlUtils;

/**
 * 
 * HttpTransportClient
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class HttpTransportClient implements Executable, TransportClient {

	private static final Log logger = LogFactory.getLog(HttpTransportClient.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String brokerUrl;
	private final HttpClient httpClient;
	private final Partitioner partitioner;
	private final int startupDelay;
	private volatile boolean started;

	public HttpTransportClient(String brokerUrl, HttpClient httpClient, Partitioner partitioner, int startupDelay) {
		this.brokerUrl = brokerUrl;
		this.httpClient = httpClient;
		this.partitioner = partitioner;
		this.startupDelay = startupDelay;
	}

	@Override
	public void send(CharSequence json) {
		send(Tuple.byString(json.toString()));
	}

	@Override
	public void send(Tuple tuple) {
		if (started && httpClient.isOpened()) {
			httpClient.send(tuple, partitioner);
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
		httpClient.open();
		doConnect();
		ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.MINUTES);
		logger.info(this + "\tStart HttpTransportClient ok.");
	}

	@Override
	public void close() {
		if (!started) {
			throw new IllegalStateException("HttpTransportClient is not started now.");
		}
		started = false;
		httpClient.close();
		logger.info("Close HttpTransportClient ok.");
	}

	private void doConnect() {
		String[] channels = getChannels();
		for (String channel : channels) {
			httpClient.addChannel(channel);
		}
	}

	@Override
	public boolean execute() {
		doConnect();
		return started && httpClient.isOpened();
	}

	@Override
	public String[] getChannels() {
		String content;
		try {
			content = UrlUtils.toString(brokerUrl, "utf-8");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new String[0];
		}
		try {
			return objectMapper.readValue(content, String[].class);
		} catch (IOException e) {
			throw new TransportClientException("Invalid format brokerUrl: " + content, e);
		}
	}

}
