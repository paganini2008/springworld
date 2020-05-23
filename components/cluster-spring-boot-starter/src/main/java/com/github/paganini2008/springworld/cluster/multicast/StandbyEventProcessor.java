package com.github.paganini2008.springworld.cluster.multicast;

import static com.github.paganini2008.springworld.cluster.multicast.ContextMulticastEventNames.STANDBY;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * StandbyEventProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
public class StandbyEventProcessor implements RedisMessageHandler {

	@Autowired
	private RedisMessageSender messageSender;

	@Autowired
	private ContextMulticastGroup multicastGroup;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private ContextMulticastEventListener multicastEventListener;

	@Override
	public void onMessage(String channel, Object message) { 
		String[] args = ((String) message).split(":", 2);
		final String thatId = args[0];
		final int weight = Integer.parseInt(args[1]);
		if (!multicastGroup.hasRegistered(thatId)) {
			multicastGroup.registerChannel(thatId, weight);
			messageSender.sendMessage(STANDBY, clusterId.get() + ":" + clusterId.getWeight());

			multicastEventListener.fireOnJoin(thatId);
		}
	}

	@Override
	public String getChannel() {
		return STANDBY;
	}

}
