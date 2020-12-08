package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.springdessert.cluster.ClusterState;
import com.github.paganini2008.springdessert.cluster.LeaderContext;
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
	private final LeaderContext leaderContext;
	private final RequestProcessor requestProcessor;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RestClientBeanAspect(RestClient restClient, LeaderContext leaderContext, RequestProcessor requestProcessor,
			RequestInterceptorContainer requestInterceptorContainer) {
		this.restClient = restClient;
		this.leaderContext = leaderContext;
		this.requestProcessor = requestProcessor;
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
		String path = api.path();
		int timeout = api.timeout() > 0 ? api.timeout() : restClient.timeout();
		int retries = api.retries() > 0 ? api.retries() : restClient.retries();
		int concurrency = api.concurrency() > 0 ? api.concurrency() : restClient.concurrency();
		if (concurrency < 1) {
			concurrency = Integer.MAX_VALUE;
		}

		HttpMethod httpMethod = api.method();
		String[] headers = api.headers();
		ParameterAnnotationRequest request = new ParameterAnnotationRequest(path, httpMethod);
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
				responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(request, responseType, concurrency, retries, timeout);
			} else if (retries < 1 && timeout > 0) {
				responseEntity = requestProcessor.sendRequestWithTimeout(request, responseType, concurrency, timeout);
			} else if (retries > 0 && timeout < 1) {
				responseEntity = requestProcessor.sendRequestWithRetry(request, responseType, concurrency, retries);
			} else {
				responseEntity = requestProcessor.sendRequest(request, responseType, concurrency);
			}
		} catch (RestClientException e) {
			FallbackProvider fallback = getFallback(api.fallback(), restClient.fallback());
			if (fallback != null) {
				try {
					responseEntity = executeFallback(fallback, method, args, e, api.fallbackException(), api.fallbackHttpStatus());
					log.error(e.getMessage(), e);
				} catch (Exception fallbackError) {
					reason = fallbackError instanceof RestClientException ? (RestClientException) fallbackError
							: new RestfulException(fallbackError.getMessage(), fallbackError, request);
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
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
		throw new RestClientException("Illegal request: " + request.toString());
	}

	private FallbackProvider getFallback(Class<?> fallbackClass, Class<?> defaultFallbackClass) {
		try {
			if (fallbackClass != null) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(fallbackClass);
			} else if (defaultFallbackClass != null) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(defaultFallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private ResponseEntity<?> executeFallback(FallbackProvider fallback, Method method, Object[] arguments, RestClientException e,
			Class<?>[] exceptionClasses, HttpStatus[] httpStatuses) {
		if (e instanceof RestfulException) {
			RestfulException restClientException = (RestfulException) e;
			for (Class<?> cls : exceptionClasses) {
				if (restClientException.getCause() != null && cls.isInstance(restClientException.getCause())) {
					return wrapResponse(fallback, method, arguments, restClientException);
				}
			}
		} else if (e instanceof HttpStatusCodeException) {
			HttpStatusCodeException restClientException = (HttpStatusCodeException) e;
			for (HttpStatus status : httpStatuses) {
				if (restClientException.getStatusCode() != null && restClientException.getStatusCode() == status) {
					return wrapResponse(fallback, method, arguments, restClientException);
				}
			}
		}
		throw e;
	}

	private ResponseEntity<?> wrapResponse(FallbackProvider fallback, Method method, Object[] arguments,
			RestClientException restClientException) {
		Object body = fallback.getBody(restClient.provider(), method, arguments, restClientException);
		return new ResponseEntity<>(body, fallback.getHeaders(), fallback.getHttpStatus());
	}

	@Override
	public void catchException(Object target, Method method, Object[] args, Throwable e) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
	}

}
