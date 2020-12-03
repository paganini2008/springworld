package com.github.paganini2008.springdessert.cluster.http;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Utf8CharsetRestTemplate
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class Utf8CharsetRestTemplate extends RestTemplate {

	public Utf8CharsetRestTemplate() {
		super();
		applySettings();
	}

	public Utf8CharsetRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
		super(clientHttpRequestFactory);
		applySettings();
	}

	public Utf8CharsetRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
		applySettings();
	}

	private void applySettings() {
		final List<HttpMessageConverter<?>> messageConverters = getMessageConverters();
		for (int i = 0; i < messageConverters.size(); i++) {
			HttpMessageConverter<?> httpMessageConverter = messageConverters.get(i);
			if (httpMessageConverter instanceof StringHttpMessageConverter) {
				messageConverters.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			}
		}
	}

}
