package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.MulticastGroupEvent.EventType;

/**
 * 
 * MulticastListenerContainer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class MulticastListenerContainer implements ApplicationContextAware, BeanPostProcessor {

	private final List<MulticastGroupListener> listeners = new CopyOnWriteArrayList<MulticastGroupListener>();
	private final Map<String, List<MulticastMessageListener>> topicListeners = new ConcurrentHashMap<String, List<MulticastMessageListener>>();
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void fireOnActive(final ApplicationInfo applicationInfo) {
		List<MulticastGroupListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onActive(applicationInfo);
			});
		}
		applicationContext.publishEvent(new MulticastGroupEvent(applicationContext, applicationInfo, EventType.ON_ACTIVE));
	}

	public void fireOnInactive(final ApplicationInfo applicationInfo) {
		List<MulticastGroupListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onInactive(applicationInfo);
			});
		}
		applicationContext.publishEvent(new MulticastGroupEvent(applicationContext, applicationInfo, EventType.ON_INACTIVE));
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final Object message) {
		List<MulticastGroupListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onMessage(applicationInfo, message);
			});
		}
		MulticastGroupEvent event = new MulticastGroupEvent(applicationContext, applicationInfo, EventType.ON_MESSAGE);
		event.setMessage(message);
		applicationContext.publishEvent(event);
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final String topic, final String id, final Object message) {
		List<MulticastMessageListener> eventListeners = topicListeners.get(topic);
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onMessage(applicationInfo, id, message);
			});
		}
	}

	public void registerListener(MulticastMessageListener eventListener) {
		final String topic = eventListener.getTopic();
		List<MulticastMessageListener> eventListeners = MapUtils.get(topicListeners, topic, () -> {
			return new CopyOnWriteArrayList<MulticastMessageListener>();
		});
		if (!eventListeners.contains(eventListener)) {
			eventListeners.add(eventListener);
		}
	}

	public void unregisterListener(MulticastMessageListener eventListener) {
		final String topic = eventListener.getTopic();
		List<MulticastMessageListener> eventListeners = topicListeners.get(topic);
		if (eventListeners != null && eventListeners.contains(eventListener)) {
			eventListeners.remove(eventListener);
			if (eventListeners.isEmpty()) {
				topicListeners.remove(topic);
			}
		}
	}

	public void registerListener(MulticastGroupListener eventListener) {
		if (!listeners.contains(eventListener)) {
			listeners.add(eventListener);
		}
	}

	public void unregisterListener(MulticastGroupListener eventListener) {
		if (listeners.contains(eventListener)) {
			listeners.remove(eventListener);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof MulticastGroupListener) {
			registerListener((MulticastGroupListener) bean);
		} else if (bean instanceof MulticastMessageListener) {
			registerListener((MulticastMessageListener) bean);
		}
		return bean;
	}

}
