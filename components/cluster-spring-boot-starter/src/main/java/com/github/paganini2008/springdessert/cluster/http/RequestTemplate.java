package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.collection.MultiMappedMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RequestTemplate
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class RequestTemplate implements BeanPostProcessor {

	private final List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<RequestInterceptor>();
	private final MultiMappedMap<String, String, Permit> requestPermitMap = new MultiMappedMap<String, String, Permit>();
	private final RequestProcessor requestProcessor;

	public RequestTemplate(RequestProcessor requestProcessor) {
		this.requestProcessor = requestProcessor;
	}

	public ResponseEntity<Object> sendRequest(String provider, Request req, Type responseType) {
		ResponseEntity<Object> responseEntity = null;
		RestClientException reason = null;
		AbstractRequest request = (AbstractRequest) req;
		String path = request.getPath();
		int retries = (Integer) request.getAttribute("retries");
		int timeout = (Integer) request.getAttribute("timeout");
		int permits = (Integer) request.getAttribute("permits");
		FallbackProvider fallbackProvider = (FallbackProvider) request.getAttribute("fallback");

		Permit permit = requestPermitMap.get(provider, path, () -> {
			return new Permit(permits);
		});
		try {
			if (permit.availablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.BLOCKED);
			}
			permit.accquire();
			if (beforeSubmit(provider, request)) {
				if (retries > 0 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(provider, request, responseType, retries, timeout);
				} else if (retries < 1 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithTimeout(provider, request, responseType, timeout);
				} else if (retries > 0 && timeout < 1) {
					responseEntity = requestProcessor.sendRequestWithRetry(provider, request, responseType, retries);
				} else {
					responseEntity = requestProcessor.sendRequest(provider, request, responseType);
				}
			}
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			reason = e;
			responseEntity = executeFallback(provider, request, responseType, e, fallbackProvider);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			permit.release();
			afterSubmit(provider, request, responseEntity, reason);
		}
		if (responseEntity == null) {
			responseEntity = executeFallback(provider, request, responseType, null, fallbackProvider);
		}
		return responseEntity;
	}

	protected ResponseEntity<Object> executeFallback(String provider, Request request, Type responseType, RestClientException e,
			FallbackProvider fallback) {
		if (fallback == null) {
			throw e;
		}
		try {
			if (fallback.hasFallback(provider, request, responseType, e)) {
				Object body = fallback.getBody(provider, request, responseType, e);
				return new ResponseEntity<Object>(body, fallback.getHeaders(), fallback.getHttpStatus());
			}
		} catch (Exception fallbackError) {
			throw ExceptionUtils.wrapException("Failed to execute fallback", fallbackError, request);
		}
		throw e;
	}

	public static class Permit {

		private final AtomicInteger counter;
		private final int maxPermits;

		Permit(int maxPermits) {
			this.counter = new AtomicInteger(0);
			this.maxPermits = maxPermits;
		}

		public int accquire() {
			return counter.incrementAndGet();
		}

		public int accquire(int permits) {
			return counter.addAndGet(permits);
		}

		public int release() {
			return counter.decrementAndGet();
		}

		public int release(int permits) {
			return counter.addAndGet(-permits);
		}

		public int availablePermits() {
			return maxPermits - counter.get();
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RequestInterceptor) {
			addInterceptor((RequestInterceptor) bean);
		}
		return bean;
	}

	public void addInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}

	public void removeInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.remove(interceptor);
		}
	}

	public boolean beforeSubmit(String provider, Request request) {
		boolean proceeded = true;
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				proceeded &= interceptor.beforeSubmit(provider, request);
			}
		}
		return proceeded;
	}

	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable reason) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				interceptor.afterSubmit(provider, request, responseEntity, reason);
			}
		}
	}

}
