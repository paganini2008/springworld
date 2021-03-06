package com.github.paganini2008.springdessert.xtransport.transport;

import com.github.paganini2008.embeddedio.Channel;
import com.github.paganini2008.xtransport.ChannelEvent;
import com.github.paganini2008.xtransport.ChannelEventListener;
import com.github.paganini2008.xtransport.ChannelEvent.EventType;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedChannelEventListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public class EmbeddedChannelEventListener implements ChannelEventListener<Channel> {

	@Override
	public void fireChannelEvent(ChannelEvent<Channel> channelEvent) {
		if (log.isTraceEnabled()) {
			Channel channel = channelEvent.getSource();
			EventType eventType = channelEvent.getEventType();
			switch (eventType) {
			case CONNECTED:
				log.trace(channel.getRemoteAddr() + " has established connection.");
				break;
			case CLOSED:
				log.trace(channel.getRemoteAddr() + " has loss connection.");
				break;
			case PING:
				log.trace(channel.getRemoteAddr() + " send a ping.");
				break;
			case PONG:
				log.trace(channel.getRemoteAddr() + " send a pong.");
				break;
			case FAULTY:
				log.trace(channel.getRemoteAddr() + " has loss connection for fatal reason.", channelEvent.getCause());
				break;
			}
		}
	}

}
