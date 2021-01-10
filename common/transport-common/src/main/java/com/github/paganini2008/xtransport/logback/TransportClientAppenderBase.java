package com.github.paganini2008.xtransport.logback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.xtransport.Env;
import com.github.paganini2008.xtransport.TransportClient;
import com.github.paganini2008.xtransport.Tuple;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * 
 * TransportClientAppenderBase
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class TransportClientAppenderBase extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private TransportClient transportClient;
	private String clusterName = "default";
	private String applicationName;
	private String host = NetUtils.getLocalHost();
	private String identifier = String.valueOf(Env.getPid());

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (transportClient == null) {
			return;
		}
		Tuple tuple = Tuple.newOne();
		tuple.setField("clusterName", clusterName);
		tuple.setField("applicationName", applicationName);
		tuple.setField("host", host);
		tuple.setField("identifier", identifier);
		tuple.setField("loggerName", eventObject.getLoggerName());
		tuple.setField("message", eventObject.getFormattedMessage());
		tuple.setField("level", eventObject.getLevel().toString());
		String reason = ThrowableProxyUtil.asString(eventObject.getThrowableProxy());
		if (StringUtils.isNotBlank(reason)) {
			//reason = reason.replace(CoreConstants.LINE_SEPARATOR, " <br/> ");
		}
		tuple.setField("reason", reason);
		Map<String, String> mdc = eventObject.getMDCPropertyMap();
		tuple.setField("mdc", MapUtils.isNotEmpty(mdc) ? new HashMap<String, String>(mdc) : Collections.EMPTY_MAP);
		tuple.setField("marker", eventObject.getMarker() != null ? eventObject.getMarker().getName() : "");
		tuple.setField("timestamp", eventObject.getTimeStamp());

		transportClient.send(tuple);
	}

	@Override
	public final void start() {
		this.transportClient = buildTransportClient();
		super.start();
	}

	protected abstract TransportClient buildTransportClient();

}
