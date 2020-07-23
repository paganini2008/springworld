package com.github.paganini2008.transport.logging;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.transport.HashPartitioner;
import com.github.paganini2008.transport.HttpClient;
import com.github.paganini2008.transport.HttpTransportClient;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.TcpTransportClient;
import com.github.paganini2008.transport.TransportClient;
import com.github.paganini2008.transport.Tuple;

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

	private static final Log logger = LogFactory.getLog(LogbackTransportClientAppender.class);
	private String nioClientClassName = "com.github.paganini2008.transport.netty.NettyClient";
	private String httpClientClassName = "com.github.paganini2008.transport.DefaultHttpClient";
	private String brokerProtocol = "tcp";
	private String brokerUrl;
	private int startupDelay = 0;
	private String partitionerClassName = "com.github.paganini2008.transport.RoundRobinPartitioner";
	private String groupingFields;
	private int httpConnectionTimeout = 60;
	private int httpReadTimeout = 60;
	private TransportClient transportClient;

	public void setNioClient(String nioClientClassName) {
		this.nioClientClassName = nioClientClassName;
	}

	public void setHttpClient(String httpClientClassName) {
		this.httpClientClassName = httpClientClassName;
	}

	public void setBrokerProtocol(String brokerProtocol) {
		this.brokerProtocol = brokerProtocol;
	}

	public void setPartitioner(String partitioner) {
		this.partitionerClassName = partitioner;
	}

	public void setStartupDelay(int startupDelay) {
		this.startupDelay = startupDelay;
	}

	public void setGroupingFields(String groupingFields) {
		this.groupingFields = groupingFields;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public void setHttpConnectionTimeout(int httpConnectionTimeout) {
		this.httpConnectionTimeout = httpConnectionTimeout;
	}

	public void setHttpReadTimeout(int httpReadTimeout) {
		this.httpReadTimeout = httpReadTimeout;
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		Tuple tuple = Tuple.newOne();
		tuple.setField("loggerName", eventObject.getLoggerName());
		tuple.setField("message", eventObject.getFormattedMessage());
		tuple.setField("level", eventObject.getLevel().toString());
		tuple.setField("error", ThrowableProxyUtil.asString(eventObject.getThrowableProxy()));
		tuple.setField("mdc", eventObject.getMDCPropertyMap());
		tuple.setField("marker", eventObject.getMarker() != null ? eventObject.getMarker().getName() : "");
		tuple.setField("timestamp", eventObject.getTimeStamp());
		transportClient.send(tuple);
	}

	@Override
	public synchronized void start() {
		logger.info("Start the LogbackTransportClientAppender.");

		if (!isStarted()) {
			super.start();
		}
		if (transportClient != null) {
			return;
		}

		Partitioner partitioner = BeanUtils.instantiate(partitionerClassName);
		if (partitioner instanceof HashPartitioner && StringUtils.isNotBlank(groupingFields)) {
			((HashPartitioner) partitioner).addFieldNames(groupingFields.trim().split(","));
		}
		switch (brokerProtocol.toLowerCase()) {
		case "tcp":
			NioClient nioClient = BeanUtils.instantiate(nioClientClassName);
			transportClient = new TcpTransportClient(brokerUrl, nioClient, partitioner, startupDelay);
			break;
		case "https":
		case "http":
			HttpClient httpClient = BeanUtils.instantiate(httpClientClassName);
			transportClient = new HttpTransportClient(brokerUrl, httpClient, partitioner, startupDelay);
			break;
		default:
			throw new UnsupportedOperationException("Unkown broker protocol: " + brokerProtocol);
		}
		transportClient.start();
	}

	@Override
	public void stop() {
		super.stop();
		logger.info("Stop the LogbackTransportClientAppender.");
	}

}
