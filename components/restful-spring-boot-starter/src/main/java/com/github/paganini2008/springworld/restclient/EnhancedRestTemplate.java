package com.github.paganini2008.springworld.restclient;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * EnhancedRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EnhancedRestTemplate extends RestTemplate {

	public <T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Object... uriVariables) {
		RequestCallback requestCallback = super.httpEntityCallback(requestBody, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = super.responseEntityExtractor(responseType);
		return super.execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType,
			Map<String, Object> uriParameters) {
		RequestCallback requestCallback = super.httpEntityCallback(requestBody, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = super.responseEntityExtractor(responseType);
		return super.execute(url, method, requestCallback, responseExtractor, uriParameters);
	}

}
