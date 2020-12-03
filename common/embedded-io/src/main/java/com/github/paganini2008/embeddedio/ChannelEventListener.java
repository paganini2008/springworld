package com.github.paganini2008.embeddedio;

import com.github.paganini2008.embeddedio.ChannelEvent.EventType;

/**
 * 
 * ChannelEventListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface ChannelEventListener {
	
	void onEventFired(ChannelEvent event);

	default EventType getEventType() {
		return EventType.ALL;
	}

}
