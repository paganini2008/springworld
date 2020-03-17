package com.github.paganini2008.transport.logging;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.transport.HashPartitioner;
import com.github.paganini2008.transport.JedisLookupAddress;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.RandomPartitioner;
import com.github.paganini2008.transport.RoundRobinPartitioner;
import com.github.paganini2008.transport.TransportClient;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.grizzly.GrizzlyClient;
import com.github.paganini2008.transport.mina.MinaClient;
import com.github.paganini2008.transport.netty.NettyClient;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * 
 * LogbackTransportClientAppender
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class LogbackTransportClientAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String transporter = "netty";
	private String redisHost = "localhost";
	private int redisPort = 6379;
	private int redisDbIndex = 0;
	private String password = "";
	private String clusterName;
	private int startupDelay = 0;
	private String partitioner = "roundrobin";
	private String groupingFieldName;
	private TransportClient transportClient;

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public void setTransporter(String transporter) {
		this.transporter = transporter;
	}

	public void setRedisDbIndex(int redisDbIndex) {
		this.redisDbIndex = redisDbIndex;
	}

	public void setPartitioner(String partitioner) {
		this.partitioner = partitioner;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setStartupDelay(int startupDelay) {
		this.startupDelay = startupDelay;
	}

	public void setGroupingFieldName(String groupingFieldName) {
		this.groupingFieldName = groupingFieldName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		Tuple tuple = Tuple.newOne();
		tuple.setField("loggerName", eventObject.getLoggerName());
		tuple.setField("message", eventObject.getFormattedMessage());
		tuple.setField("level", eventObject.getLevel().levelStr);
		tuple.setField("error", ThrowableProxyUtil.asString(eventObject.getThrowableProxy()));
		tuple.setField("mdc", eventObject.getMDCPropertyMap());
		tuple.setField("marker", eventObject.getMarker().getName());
		tuple.setField("timestamp", eventObject.getTimeStamp());
		transportClient.send(tuple);
	}

	@Override
	public void start() {
		NioClient nioClient;
		switch (transporter.toLowerCase()) {
		case "netty":
			nioClient = new NettyClient();
			break;
		case "mina":
			nioClient = new MinaClient();
			break;
		case "grizzly":
			nioClient = new GrizzlyClient();
			break;
		default:
			throw new IllegalArgumentException("Unknown transporter: " + transporter);
		}

		Partitioner partitioner;
		switch (this.partitioner.toLowerCase()) {
		case "roundrobin":
			partitioner = new RoundRobinPartitioner();
			break;
		case "random":
			partitioner = new RandomPartitioner();
			break;
		case "hash":
			partitioner = new HashPartitioner();
			break;
		case "mdc-hash":
			partitioner = new MdcHashPartitioner();
			break;
		default:
			throw new IllegalArgumentException("Unknown partitioner: " + this.partitioner);
		}

		this.transportClient = new TransportClient(clusterName, new JedisLookupAddress(redisHost, redisPort, password, redisDbIndex),
				nioClient, partitioner, startupDelay);
		if (StringUtils.isNotBlank(groupingFieldName)) {
			this.transportClient.setGroupingFieldName(groupingFieldName);
		}
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		if (transportClient != null) {
			transportClient.close();
		}
	}

}
