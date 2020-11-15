package com.github.paganini2008.springworld.cluster.http;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Comparables;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.reflection.MethodUtils;

/**
 * 
 * RestClientBeanAspect
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestClientBeanAspect implements Aspect {

	private final int defaultRetries;
	private final int defaultTimeout;
	private final RequestProcessor requestProcessor;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RestClientBeanAspect(RequestProcessor requestProcessor, int defaultRetries, int defaultTimeout,
			RequestInterceptorContainer requestInterceptorContainer) {
		this.defaultRetries = defaultRetries;
		this.defaultTimeout = defaultTimeout;
		this.requestProcessor = requestProcessor;
		this.requestInterceptorContainer = requestInterceptorContainer;
	}

	@Override
	public Object call(Object fallback, Method method, Object[] args) throws Throwable {
		final Api api = method.getAnnotation(Api.class);
		String path = api.path();
		int timeout = api.timeout();
		timeout = Comparables.getOrDefault(timeout, -1, defaultTimeout);
		int retries = api.retries();
		retries = Comparables.getOrDefault(retries, 0, defaultRetries);

		HttpMethod httpMethod = api.method();
		String[] headers = api.headers();
		SimpleRequest request = new SimpleRequest(path, httpMethod);
		request.getHeaders().setContentType(MediaType.parseMediaType(api.contentType()));
		if (ArrayUtils.isNotEmpty(headers)) {
			for (String header : headers) {
				String[] headerArgs = header.split("=", 2);
				if (headerArgs.length == 2) {
					request.getHeaders().add(headerArgs[0], headerArgs[1]);
				}
			}
		}

		Type responseType = method.getGenericReturnType();
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			request.accessParameter(parameters[i], args[i]);
		}
		ResponseEntity<?> responseEntity = null;
		Throwable reason = null;
		try {
			requestInterceptorContainer.beforeSubmit(request);
			if (retries > 0 && timeout > 0) {
				responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(request, responseType, retries, timeout);
			} else if (retries < 1 && timeout > 0) {
				responseEntity = requestProcessor.sendRequestWithTimeout(request, responseType, timeout);
			} else if (retries > 0 && timeout < 1) {
				responseEntity = requestProcessor.sendRequestWithRetry(request, responseType, retries);
			} else {
				responseEntity = requestProcessor.sendRequest(request, responseType);
			}
		} catch (RestClientException e) {
			try {
				responseEntity = executeFallback(fallback, method, args, e, api.fallbackException(), api.fallbackHttpStatus());
			} catch (Exception ee) {
				reason = ee;
			}
		} catch (Exception e) {
			reason = e;
		} finally {
			requestInterceptorContainer.afterSubmit(request, responseEntity, reason);
		}
		if (responseEntity != null) {
			return responseEntity.getBody();
		}
		if (reason != null) {
			if (reason instanceof RestClientException) {
				throw (RestClientException) reason;
			} else {
				throw new RestClientException(reason.getMessage(), reason);
			}
		}
		throw new RestClientException("No result");
	}

	private ResponseEntity<?> executeFallback(Object fallback, Method method, Object[] args, RestClientException e,
			Class<?>[] exceptionClasses, HttpStatus[] httpStatuses) {
		if (e instanceof RestfulApiException) {
			RestfulApiException apiException = (RestfulApiException) e;
			for (Class<?> cls : exceptionClasses) {
				if (apiException.getCause() != null && cls.isInstance(apiException.getCause())) {
					return invokeFallbackMethod(fallback, method, args);
				}
			}
		} else if (e instanceof HttpStatusCodeException) {
			HttpStatusCodeException real = (HttpStatusCodeException) e;
			for (HttpStatus status : httpStatuses) {
				if (real.getStatusCode() != null && real.getStatusCode() == status) {
					return invokeFallbackMethod(fallback, method, args);
				}
			}

		}
		throw e;
	}

	private ResponseEntity<?> invokeFallbackMethod(Object fallback, Method method, Object[] args) {
		if (fallback != null) {
			Object result = MethodUtils.invokeMethod(fallback, method, args);
			return ResponseEntity.ok(result);
		}
		return null;
	}

}
