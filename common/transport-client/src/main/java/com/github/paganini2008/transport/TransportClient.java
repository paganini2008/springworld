package com.github.paganini2008.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.transport.netty.NettyClient;

/**
 * 
 * TransportClient
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TransportClient implements Executable {

	private static final Log logger = LogFactory.getLog(TransportClient.class);

	public TransportClient(String clusterName, String redisHost, int redisPort, String password) {
		this(clusterName, new JedisLookupAddress(redisHost, redisPort, password, 0));
	}

	public TransportClient(String clusterName, LookupAddress lookupAddress) {
		this(clusterName, lookupAddress, new NettyClient(), new RoundRobinPartitioner(), 0);
	}

	public TransportClient(String clusterName, LookupAddress lookupAddress, NioClient nioClient, Partitioner partitioner,
			int startupDelay) {
		this.clusterName = clusterName;
		this.lookupAddress = lookupAddress;
		this.nioClient = nioClient;
		this.partitioner = partitioner;
		this.startupDelay = startupDelay;
	}

	private final String clusterName;
	private final LookupAddress lookupAddress;
	private final NioClient nioClient;
	private final Partitioner partitioner;
	private final int startupDelay;
	private boolean started;

	public void setGroupingFieldName(String groupingFieldName) {
		if (partitioner instanceof HashPartitioner && StringUtils.isNotBlank(groupingFieldName)) {
			((HashPartitioner) partitioner).addFieldNames(groupingFieldName.trim().split(","));
		}
	}

	public void send(CharSequence json) {
		send(Tuple.byString(json.toString()));
	}

	public void send(Tuple tuple) {
		if (started && nioClient.isOpened()) {
			nioClient.send(tuple, partitioner);
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void start() {
		if (started) {
			throw new IllegalStateException("TransportClient is started now.");
		}
		if (StringUtils.isBlank(clusterName)) {
			throw new TransportClientException("ClusterName must be required.");
		}
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
		started = true;
		logger.info("Start TransportClient ok.");
	}

	public void close() {
		if (!started) {
			throw new IllegalStateException("TransportClient is not started now.");
		}
		started = false;
		nioClient.close();
		lookupAddress.releaseExternalResources();
		logger.info("Close TransportClient ok.");
	}

	private void doConnect() {
		String[] addresses = new String[0];
		try {
			addresses = lookupAddress.getAddresses(clusterName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		for (String address : addresses) {
			String[] args = address.split(":", 2);
			try {
				nioClient.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])), location -> {
					logger.info("Logging to: " + location);
				});
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean execute() {
		doConnect();
		return nioClient.isOpened();
	}

}
