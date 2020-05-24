package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEvent.EventType;

/**
 * 
 * ClusterMulticastEventListenerContainer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ClusterMulticastEventListenerContainer implements ApplicationContextAware {

	public static final String GLOBAL_TOPIC = "*";
	private final Map<String, List<ClusterMulticastEventListener>> topicHandlers = new ConcurrentHashMap<String, List<ClusterMulticastEventListener>>();

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void fireOnActive(final String instanceId) {
		List<ClusterMulticastEventListener> eventHandlers = topicHandlers.get(GLOBAL_TOPIC);
		if (eventHandlers != null) {
			eventHandlers.forEach(handler -> {
				handler.onActive(instanceId);
			});
		}
		applicationContext.publishEvent(new ClusterMulticastEvent(applicationContext, instanceId, EventType.ON_ACTIVE));
	}

	public void fireOnInactive(final String instanceId) {
		List<ClusterMulticastEventListener> eventHandlers = topicHandlers.get(GLOBAL_TOPIC);
		if (eventHandlers != null) {
			eventHandlers.forEach(handler -> {
				handler.onInactive(instanceId);
			});
		}
		applicationContext.publishEvent(new ClusterMulticastEvent(applicationContext, instanceId, EventType.ON_INACTIVE));
	}

	public void fireOnMessage(final String instanceId, final String topic, final Object message) {
		if (GLOBAL_TOPIC.equals(topic)) {
			List<ClusterMulticastEventListener> eventHandlers = topicHandlers.get(topic);
			if (eventHandlers != null) {
				eventHandlers.forEach(handler -> {
					handler.onGlobalMessage(instanceId, message);
				});
			}
			ClusterMulticastEvent event = new ClusterMulticastEvent(applicationContext, instanceId, EventType.ON_MESSAGE);
			event.setMessage(message);
			applicationContext.publishEvent(event);
		} else {
			List<ClusterMulticastEventListener> eventHandlers = topicHandlers.get(topic);
			if (eventHandlers != null) {
				eventHandlers.forEach(handler -> {
					handler.onMessage(instanceId, message);
				});
			}
		}
	}

	public void addListener(ClusterMulticastEventListener multicastEventHandler) {
		final String topic = multicastEventHandler.getTopic();
		List<ClusterMulticastEventListener> handlers = MapUtils.get(topicHandlers, topic, () -> {
			return new CopyOnWriteArrayList<ClusterMulticastEventListener>();
		});
		if (!handlers.contains(multicastEventHandler)) {
			handlers.add(multicastEventHandler);
		}
		handlers = MapUtils.get(topicHandlers, GLOBAL_TOPIC, () -> {
			return new CopyOnWriteArrayList<ClusterMulticastEventListener>();
		});
		if (!handlers.contains(multicastEventHandler)) {
			handlers.add(multicastEventHandler);
		}
	}

}
