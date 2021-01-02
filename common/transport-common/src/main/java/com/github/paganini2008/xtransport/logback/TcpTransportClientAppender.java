package com.github.paganini2008.xtransport.logback;

import com.github.paganini2008.xtransport.TcpTransportClient;
import com.github.paganini2008.xtransport.TransportClient;

/**
 * 
 * TcpTransportClientAppender
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class TcpTransportClientAppender extends TransportClientAppenderBase {

	private String brokerUrl;

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	@Override
	protected TransportClient buildTransportClient() {
		return new TcpTransportClient(brokerUrl);
	}

}
