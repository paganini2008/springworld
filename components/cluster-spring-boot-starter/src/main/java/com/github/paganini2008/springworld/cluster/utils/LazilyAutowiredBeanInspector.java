package com.github.paganini2008.springworld.cluster.utils;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * LazilyAutowiredBeanInspector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LazilyAutowiredBeanInspector implements ApplicationListener<ContextRefreshedEvent> {

	private final Observable lazyAutowiredObservable = Observable.unrepeatable();

	public void autowireLazily(final Object bean) {
		lazyAutowiredObservable.addObserver((ob, arg) -> {
			ApplicationContext applicationContext = (ApplicationContext) arg;
			AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
			beanFactory.autowireBean(bean);
		});
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		lazyAutowiredObservable.notifyObservers(event.getApplicationContext());
	}

}
