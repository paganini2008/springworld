package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterStateChangeEvent.EventType;

/**
 * 
 * ClusterMulticastListenerContainer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ClusterMulticastListenerContainer implements ApplicationContextAware {

	private final List<ClusterStateChangeListener> listeners = new CopyOnWriteArrayList<ClusterStateChangeListener>();
	private final Map<String, List<ClusterMessageListener>> topicListeners = new ConcurrentHashMap<String, List<ClusterMessageListener>>();
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void fireOnActive(final ApplicationInfo applicationInfo) {
		List<ClusterStateChangeListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onActive(applicationInfo);
			});
		}
		applicationContext.publishEvent(new ClusterStateChangeEvent(applicationContext, applicationInfo, EventType.ON_ACTIVE));
	}

	public void fireOnInactive(final ApplicationInfo applicationInfo) {
		List<ClusterStateChangeListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onInactive(applicationInfo);
			});
		}
		applicationContext.publishEvent(new ClusterStateChangeEvent(applicationContext, applicationInfo, EventType.ON_INACTIVE));
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final Object message) {
		List<ClusterStateChangeListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onMessage(applicationInfo, message);
			});
		}
		ClusterStateChangeEvent event = new ClusterStateChangeEvent(applicationContext, applicationInfo, EventType.ON_MESSAGE);
		event.setMessage(message);
		applicationContext.publishEvent(event);
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final String topic, final String id, final Object message) {
		List<ClusterMessageListener> eventListeners = topicListeners.get(topic);
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onMessage(applicationInfo, id, message);
			});
		}
	}

	public void registerListener(ClusterMessageListener eventListener) {
		final String topic = eventListener.getTopic();
		List<ClusterMessageListener> eventListeners = MapUtils.get(topicListeners, topic, () -> {
			return new CopyOnWriteArrayList<ClusterMessageListener>();
		});
		if (!eventListeners.contains(eventListener)) {
			eventListeners.add(eventListener);
		}
	}

	public void registerListener(ClusterStateChangeListener eventListener) {
		if (!listeners.contains(eventListener)) {
			listeners.add(eventListener);
		}
	}

}
