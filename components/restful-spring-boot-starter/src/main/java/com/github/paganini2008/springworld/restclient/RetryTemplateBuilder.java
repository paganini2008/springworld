package com.github.paganini2008.springworld.restclient;

import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 
 * RetryTemplateBuilder
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RetryTemplateBuilder {

	private RetryPolicy retryPolicy = new SimpleRetryPolicy();
	private BackOffPolicy backOffPolicy = new FixedBackOffPolicy();
	private RetryListener[] retryListeners = new RetryListener[0];

	public RetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	public RetryTemplateBuilder setRetryPolicy(int maxAttempts) {
		this.retryPolicy = maxAttempts > 0 ? new SimpleRetryPolicy(maxAttempts) : new NeverRetryPolicy();
		return this;
	}

	public RetryTemplateBuilder setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
		return this;
	}

	public BackOffPolicy getBackOffPolicy() {
		return backOffPolicy;
	}

	public RetryTemplateBuilder setBackOffPolicy(BackOffPolicy backOffPolicy) {
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryTemplateBuilder setBackOffPeriod(long backOffPeriod) {
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(backOffPeriod);
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryListener[] getRetryListeners() {
		return retryListeners;
	}

	public RetryTemplateBuilder setRetryListeners(RetryListener... retryListeners) {
		this.retryListeners = retryListeners;
		return this;
	}

	public RetryTemplate build() {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		retryTemplate.setListeners(retryListeners);
		return retryTemplate;
	}

}
