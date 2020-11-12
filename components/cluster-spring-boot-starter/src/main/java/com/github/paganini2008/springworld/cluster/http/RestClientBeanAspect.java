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

import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientBeanAspect
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RestClientBeanAspect implements Aspect {

	private final RequestProcessor requestProcessor;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RestClientBeanAspect(RequestProcessor requestProcessor, RequestInterceptorContainer requestInterceptorContainer) {
		this.requestProcessor = requestProcessor;
		this.requestInterceptorContainer = requestInterceptorContainer;
	}

	@Override
	public Object call(Object fallback, Method method, Object[] args) throws Throwable {
		final Api feature = method.getAnnotation(Api.class);
		String path = feature.path();
		int timeout = feature.timeout();
		int retries = feature.retries();
		HttpMethod httpMethod = feature.method();
		final Type responseType = method.getGenericReturnType();

		SimpleRequest request = new SimpleRequest(path, httpMethod);
		request.getHeaders().setContentType(MediaType.parseMediaType(feature.contentType()));

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
			if (responseEntity != null) {
				return responseEntity.getBody();
			}
		} catch (RestClientException e) {
			reason = e;
			return executeFallback(fallback, method, args, e, feature.fallbackException(), feature.fallbackHttpStatus());
		} catch (Exception e) {
			reason = e;
			log.error(e.getMessage(), e);
		} finally {
			requestInterceptorContainer.afterSubmit(request, responseEntity, reason);
		}
		return null;
	}

	private Object executeFallback(Object fallback, Method method, Object[] args, RestClientException e, Class<?>[] exceptionClasses,
			HttpStatus[] httpStatuses) {
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

		} else {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private Object invokeFallbackMethod(Object fallback, Method method, Object[] args) {
		if (fallback != null) {
			try {
				return MethodUtils.invokeMethod(fallback, method, args);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

}
