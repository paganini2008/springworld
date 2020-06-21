package com.github.paganini2008.springworld.cluster.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * ApplicationEventListenerAdaptor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ApplicationEventListenerAdaptor implements SmartApplicationListener {

	public ApplicationEventListenerAdaptor(@SuppressWarnings("unchecked") Class<? extends ApplicationEvent>... eventTypes) {
		if (eventTypes != null) {
			this.eventTypes.addAll(Arrays.asList(eventTypes));
		}
	}

	private final List<Class<? extends ApplicationEvent>> eventTypes = new ArrayList<Class<? extends ApplicationEvent>>();
	private final Observable ob = Observable.repeatable();

	public void addEventTypeSubclass(Class<? extends ApplicationEvent> eventType) {
		ob.addObserver((ob, arg) -> {
			
		});
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventTypes.contains(eventType);
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
