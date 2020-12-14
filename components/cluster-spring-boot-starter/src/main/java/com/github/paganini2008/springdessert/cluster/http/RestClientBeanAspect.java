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
import com.github.paganini2008.springdessert.cluster.HealthState;
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

	private final String provider;
	private final RestClient restClient;
	private final Class<?> interfaceClass;
	private final LeaderContext leaderContext;
	private final RequestTemplate requestTemplate;

	public RestClientBeanAspect(String provider, RestClient restClient, Class<?> interfaceClass, LeaderContext leaderContext,
			RequestTemplate requestTemplate) {
		this.provider = provider;
		this.restClient = restClient;
		this.interfaceClass = interfaceClass;
		this.leaderContext = leaderContext;
		this.requestTemplate = requestTemplate;
	}

	@Override
	public boolean beforeCall(Object target, Method method, Object[] args) {
		if (leaderContext.getHealthState() == HealthState.FATAL) {
			throw new ResourceAccessException("Fatal Cluster State");
		}
		return true;
	}

	@Override
	public Object call(Object proxy, Method method, Object[] args) throws Throwable {
		final Api api = method.getAnnotation(Api.class);
		final String path = api.path();
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
		request.setAttribute("timeout", Integer.min(api.timeout(), restClient.timeout()));
		request.setAttribute("retries", Integer.max(api.retries(), restClient.retries()));
		request.setAttribute("permits", Integer.min(api.permits(), restClient.permits()));
		request.setAttribute("fallback", getFallback(api.fallback(), restClient.fallback()));
		request.setAttribute("methodSignature", new MethodSignature(interfaceClass, method, args));
		try {
			ResponseEntity<Object> responseEntity = requestTemplate.sendRequest(provider, request, responseType);
			return responseEntity.getBody();
		} finally {
			request.clearAttributes();
		}
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

	@Override
	public void catchException(Object target, Method method, Object[] args, Throwable e) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		log.error(e.getMessage(), e);
	}

	/**
	 * 
	 * MethodSignature
	 *
	 * @author Jimmy Hoff
	 * @version 1.0
	 */
	public static class MethodSignature {

		private final Class<?> interfaceClass;
		private final Method method;
		private final Object[] args;

		MethodSignature(Class<?> interfaceClass, Method method, Object[] args) {
			this.interfaceClass = interfaceClass;
			this.method = method;
			this.args = args;
		}

		public Class<?> getInterfaceClass() {
			return interfaceClass;
		}

		public Method getMethod() {
			return method;
		}

		public Object[] getArgs() {
			return args;
		}

	}

}
