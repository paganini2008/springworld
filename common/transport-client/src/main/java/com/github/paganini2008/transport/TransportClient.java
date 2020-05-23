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

	public TransportClient(ClusterInfo clusterInfo) {
		this(clusterInfo, new NettyClient(), new RoundRobinPartitioner(), 0);
	}

	public TransportClient(ClusterInfo clusterInfo, NioClient nioClient, Partitioner partitioner, int startupDelay) {
		this.clusterInfo = clusterInfo;
		this.nioClient = nioClient;
		this.partitioner = partitioner;
		this.startupDelay = startupDelay;
	}

	private final ClusterInfo clusterInfo;
	private final NioClient nioClient;
	private final Partitioner partitioner;
	private final int startupDelay;
	private volatile boolean started;

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
		clusterInfo.releaseExternalResources();
		logger.info("Close TransportClient ok.");
	}

	private void doConnect() {
		String[] addresses = new String[0];
		try {
			addresses = clusterInfo.getInstanceAddresses();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		for (String address : addresses) {
			String[] args = address.split(":", 2);
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

	public ClusterInfo getClusterInfo() {
		return clusterInfo;
	}

}
