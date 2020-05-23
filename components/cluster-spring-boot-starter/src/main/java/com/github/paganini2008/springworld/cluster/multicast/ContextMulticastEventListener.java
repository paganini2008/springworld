package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * ContextMulticastEventListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ContextMulticastEventListener {

	public static final String GLOBAL_TOPIC = "*";
	private final Map<String, List<ContextMulticastEventHandler>> topicHandlers = new ConcurrentHashMap<String, List<ContextMulticastEventHandler>>();

	public void fireOnJoin(final String instanceId) {
		List<ContextMulticastEventHandler> eventHandlers = topicHandlers.get(GLOBAL_TOPIC);
		if (eventHandlers != null) {
			eventHandlers.forEach(handler -> {
				handler.onJoin(instanceId);
			});
		}
	}

	public void fireOnLeave(final String instanceId) {
		List<ContextMulticastEventHandler> eventHandlers = topicHandlers.get(GLOBAL_TOPIC);
		if (eventHandlers != null) {
			eventHandlers.forEach(handler -> {
				handler.onLeave(instanceId);
			});
		}
	}

	public void fireOnMessage(final String instanceId, final String topic, final Object message) {
		if (GLOBAL_TOPIC.equals(topic)) {
			List<ContextMulticastEventHandler> eventHandlers = topicHandlers.get(topic);
			if (eventHandlers != null) {
				eventHandlers.forEach(handler -> {
					handler.onGlobalMessage(instanceId, message);
				});
			}
		} else {
			List<ContextMulticastEventHandler> eventHandlers = topicHandlers.get(topic);
			if (eventHandlers != null) {
				eventHandlers.forEach(handler -> {
					handler.onMessage(instanceId, message);
				});
			}
		}
	}

	public void addHandler(ContextMulticastEventHandler multicastEventHandler) {
		final String topic = multicastEventHandler.getTopic();
		List<ContextMulticastEventHandler> handlers = MapUtils.get(topicHandlers, topic, () -> {
			return new CopyOnWriteArrayList<ContextMulticastEventHandler>();
		});
		if (!handlers.contains(multicastEventHandler)) {
			handlers.add(multicastEventHandler);
		}
		handlers = MapUtils.get(topicHandlers, GLOBAL_TOPIC, () -> {
			return new CopyOnWriteArrayList<ContextMulticastEventHandler>();
		});
		if (!handlers.contains(multicastEventHandler)) {
			handlers.add(multicastEventHandler);
		}
	}

}
