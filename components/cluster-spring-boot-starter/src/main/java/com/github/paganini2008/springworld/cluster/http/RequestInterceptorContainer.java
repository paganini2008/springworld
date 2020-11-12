package com.github.paganini2008.springworld.cluster.http;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.ResponseEntity;

/**
 * 
 * RequestInterceptorContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RequestInterceptorContainer implements BeanPostProcessor {

	private List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<RequestInterceptor>();

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

	public void beforeSubmit(Request request) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(request)) {
				interceptor.beforeSubmit(request);
			}
		}
	}

	public void afterSubmit(Request request, ResponseEntity<?> responseEntity, Throwable reason) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(request)) {
				interceptor.afterSubmit(request, responseEntity, reason);
			}
		}
	}

}
