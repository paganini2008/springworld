package com.github.paganini2008.springdessert.xtransport.transport;

import com.github.paganini2008.xtransport.ChannelEvent;
import com.github.paganini2008.xtransport.ChannelEventListener;
import com.github.paganini2008.xtransport.ChannelEvent.EventType;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyChannelEventListener
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
@Slf4j
public class NettyChannelEventListener implements ChannelEventListener<Channel> {

	@Override
	public void fireChannelEvent(ChannelEvent<Channel> channelEvent) {
		if (log.isTraceEnabled()) {
			Channel channel = channelEvent.getSource();
			EventType eventType = channelEvent.getEventType();
			switch (eventType) {
			case CONNECTED:
				log.trace(channel.remoteAddress() + " has established connection.");
				break;
			case CLOSED:
				log.trace(channel.remoteAddress() + " has loss connection.");
				break;
			case PING:
				log.trace(channel.remoteAddress() + " send a ping.");
				break;
			case PONG:
				log.trace(channel.remoteAddress() + " send a pong.");
				break;
			case FAULTY:
				log.trace(channel.remoteAddress() + " has loss connection for fatal reason.", channelEvent.getCause());
				break;
			}
		}
	}

}
