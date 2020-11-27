package com.github.paganini2008.springdessert.config;

import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

/**
 * 
 * BeanObjectChangeEvent
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class BeanObjectChangeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4317527666421114782L;

	public BeanObjectChangeEvent(Object bean, String beanName) {
		super(bean);
		this.beanName = beanName;
	}

	private final String beanName;

	public String getBeanName() {
		return beanName;
	}

	public Class<?> getBeanClass() {
		return getSource().getClass();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, new String[] { "source" });
	}

}
