package com.github.paganini2008.xtransport.logback;

import com.github.paganini2008.xtransport.TransportClient;
import com.github.paganini2008.xtransport.Tuple;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
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

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (transportClient == null) {
			return;
		}
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
	public final void start() {
		this.transportClient = buildTransportClient();
		super.start();
	}

	protected abstract TransportClient buildTransportClient();

}
