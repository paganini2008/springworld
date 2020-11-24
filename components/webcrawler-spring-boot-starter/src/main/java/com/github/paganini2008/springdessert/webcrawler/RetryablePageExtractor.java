package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RetryablePageExtractor
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class RetryablePageExtractor implements PageExtractor, RetryListener {

	private final PageExtractor pageExtractor;
	private final RetryTemplate retryTemplate;

	public RetryablePageExtractor(PageExtractor pageExtractor) {
		this(pageExtractor, 3);
	}

	public RetryablePageExtractor(PageExtractor pageExtractor, int maxAttempts) {
		this.pageExtractor = pageExtractor;
		this.retryTemplate = createRetryTemplate(maxAttempts);
	}

	@Override
	public String extractHtml(final String url) throws Exception {
		return retryTemplate.execute(context -> {
			return pageExtractor.extractHtml(url);
		}, context -> {
			return "";
		});
	}

	protected RetryTemplate createRetryTemplate(int maxAttempts) {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(maxAttempts));
		retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
		return retryTemplate;
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		if (log.isTraceEnabled()) {
			log.trace("Start to extract page html.");
		}
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (log.isTraceEnabled()) {
			log.trace("Complete to extract page html. Retry: {}", context.getRetryCount());
		}
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable e) {
		if (log.isErrorEnabled()) {
			log.error(e.getMessage(), e);
		}
	}

}
