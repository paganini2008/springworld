package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

/**
 * 
 * EnhancedRestTemplate
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class EnhancedRestTemplate extends CharsetDefinedRestTemplate {

	public EnhancedRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory, Charset charset) {
		super(clientHttpRequestFactory, charset);
	}

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
