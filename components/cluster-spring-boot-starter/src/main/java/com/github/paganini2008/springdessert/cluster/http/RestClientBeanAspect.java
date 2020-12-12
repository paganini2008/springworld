package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.springdessert.cluster.ClusterState;
import com.github.paganini2008.springdessert.cluster.LeaderContext;
import com.github.paganini2008.springdessert.cluster.http.RequestSemaphore.Permit;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientBeanAspect
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class RestClientBeanAspect implements Aspect {

	private final RestClient restClient;
	private final Class<?> interfaceClass;
	private final LeaderContext leaderContext;
	private final RequestProcessor requestProcessor;
	private final RequestSemaphore requestSemaphore;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RestClientBeanAspect(RestClient restClient, Class<?> interfaceClass, LeaderContext leaderContext,
			RequestProcessor requestProcessor, RequestSemaphore requestSemaphore, RequestInterceptorContainer requestInterceptorContainer) {
		this.restClient = restClient;
		this.interfaceClass = interfaceClass;
		this.leaderContext = leaderContext;
		this.requestProcessor = requestProcessor;
		this.requestSemaphore = requestSemaphore;
		this.requestInterceptorContainer = requestInterceptorContainer;
	}

	@Override
	public boolean beforeCall(Object target, Method method, Object[] args) {
		if (leaderContext.getClusterState() == ClusterState.FATAL) {
			throw new ResourceAccessException("Fatal Cluster State");
		}
		return true;
	}

	@Override
	public Object call(Object proxy, Method method, Object[] args) throws Throwable {
		final Api api = method.getAnnotation(Api.class);
		final String path = api.path();
		int timeout = Integer.min(api.timeout(), restClient.timeout());
		int retries = Integer.max(api.retries(), restClient.retries());
		HttpMethod httpMethod = api.method();
		String[] headers = api.headers();
		ParameterizedRequestImpl request = new ParameterizedRequestImpl(path, httpMethod);
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
		RestClientException reason = null;
		Permit permit = requestSemaphore.getPermit(restClient.provider(), restClient.permits(), path, api.permits());
		try {
			if (permit.availablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.BLOCKED);
			}
			permit.accquire();
			if (requestInterceptorContainer.beforeSubmit(restClient.provider(), request)) {
				if (retries > 0 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(request, responseType, retries, timeout);
				} else if (retries < 1 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithTimeout(request, responseType, timeout);
				} else if (retries > 0 && timeout < 1) {
					responseEntity = requestProcessor.sendRequestWithRetry(request, responseType, retries);
				} else {
					responseEntity = requestProcessor.sendRequest(request, responseType);
				}
			}
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);

			reason = e;
			FallbackProvider fallback = getFallback(api.fallback(), restClient.fallback());
			if (fallback != null) {
				try {
					if (fallback.hasFallback(restClient.provider(), interfaceClass, method, args, e)) {
						responseEntity = executeFallback(fallback, method, args, e);
					}
				} catch (Throwable fallbackError) {
					throw ExceptionUtils.wrapException("Failed to execute fallback", fallbackError, request);
				}
			} else {
				throw e;
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			permit.release();
			requestInterceptorContainer.afterSubmit(restClient.provider(), request, responseEntity, reason);
		}
		if (responseEntity == null) {
			FallbackProvider fallback = getFallback(api.fallback(), restClient.fallback());
			if (fallback != null) {
				try {
					if (fallback.hasFallback(restClient.provider(), interfaceClass, method, args, null)) {
						responseEntity = executeFallback(fallback, method, args, null);
					}
				} catch (Throwable fallbackError) {
					throw ExceptionUtils.wrapException("Failed to execute fallback", fallbackError, request);
				}
			}
		}
		return responseEntity.getBody();
	}

	private FallbackProvider getFallback(Class<?> fallbackClass, Class<?> defaultFallbackClass) {
		try {
			if (fallbackClass != null && fallbackClass != Void.class && fallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(fallbackClass);
			} else if (defaultFallbackClass != null && defaultFallbackClass != Void.class && defaultFallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(defaultFallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private ResponseEntity<?> executeFallback(FallbackProvider fallback, Method method, Object[] arguments,
			RestClientException restClientException) {
		Object body = fallback.getBody(restClient.provider(), interfaceClass, method, arguments, restClientException);
		return new ResponseEntity<>(body, fallback.getHeaders(), fallback.getHttpStatus());
	}

	@Override
	public void catchException(Object target, Method method, Object[] args, Throwable e) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		log.error(e.getMessage(), e);
	}

}
