package com.github.paganini2008.springworld.cluster.http;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 
 * RetryTemplateFactory
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RetryTemplateFactory implements BeanPostProcessor {

	private RetryPolicy retryPolicy = new SimpleRetryPolicy();
	private BackOffPolicy backOffPolicy = new FixedBackOffPolicy();
	private List<RetryListener> retryListeners = new ArrayList<RetryListener>();

	public RetryTemplateFactory setRetryPolicy(int maxAttempts) {
		this.retryPolicy = maxAttempts > 0 ? new SimpleRetryPolicy(maxAttempts) : new NeverRetryPolicy();
		return this;
	}

	public RetryTemplateFactory setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
		return this;
	}

	public RetryTemplateFactory setBackOffPolicy(BackOffPolicy backOffPolicy) {
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryTemplateFactory setBackOffPeriod(long backOffPeriod) {
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(backOffPeriod);
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryTemplateFactory addRetryListener(RetryListener retryListener) {
		if (retryListener != null) {
			this.retryListeners.add(retryListener);
		}
		return this;
	}

	public RetryTemplate createObject() {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		retryTemplate.setListeners(retryListeners.toArray(new RetryListener[0]));
		return retryTemplate;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RetryListener) {
			addRetryListener((RetryListener) bean);
		}
		return bean;
	}

}
