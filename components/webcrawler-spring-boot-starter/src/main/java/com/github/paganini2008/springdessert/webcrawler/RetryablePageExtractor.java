package com.github.paganini2008.springdessert.webcrawler;

import java.nio.charset.Charset;

import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RetryablePageExtractor
 *
 * @author Jimmy Hoff
 * 
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
	public String extractHtml(final String refer, final String url, final Charset pageEncoding) throws Exception {
		return retryTemplate.execute(context -> {
			return pageExtractor.extractHtml(refer, url, pageEncoding);
		}, context -> {
			Throwable e = context.getLastThrowable();
			if (e instanceof PageExtractorException) {
				throw (PageExtractorException) e;
			}
			throw new PageExtractorException(url, HttpStatus.FOUND);
		});
	}

	protected RetryTemplate createRetryTemplate(int maxAttempts) {
		RetryTemplate retryTemplate = new RetryTemplate();
		RetryPolicy retryPolicy = maxAttempts > 0 ? new SimpleRetryPolicy(maxAttempts) : new NeverRetryPolicy();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
		retryTemplate.setListeners(new RetryListener[] { this });
		return retryTemplate;
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		if (log.isTraceEnabled()) {
			log.trace("Start to extract page html with retry.");
		}
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (log.isTraceEnabled()) {
			log.trace("Complete to extract page html. Retry count: {}", context.getRetryCount());
		}
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable e) {
	}

}
