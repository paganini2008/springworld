package com.github.paganini2008.springdessert.cluster.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.ObjectUtils;

/**
 * 
 * ParameterAnnotationRequest
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class ParameterAnnotationRequest implements Request {

	private final String path;
	private final HttpMethod method;
	private final HttpHeaders headers;
	private final long timestamp;
	private HttpEntity<Object> body;
	private Map<String, Object> requestParameters = new HashMap<String, Object>();
	private Map<String, Object> pathVariables = new HashMap<String, Object>();

	ParameterAnnotationRequest(String path, HttpMethod method) {
		this(path, method, new HttpHeaders());
	}

	ParameterAnnotationRequest(String path, HttpMethod method, MultiValueMap<String, String> headers) {
		this.path = path;
		this.method = method;
		this.headers = new HttpHeaders(headers);
		this.timestamp = System.currentTimeMillis();
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public Map<String, Object> getRequestParameters() {
		return requestParameters;
	}

	public Map<String, Object> getPathVariables() {
		return pathVariables;
	}

	public HttpEntity<Object> getBody() {
		return body;
	}

	public void accessParameter(Parameter parameter, @Nullable Object argument) {
		String parameterName;
		Annotation[] annotations = parameter.getAnnotations();
		if (ArrayUtils.isNotEmpty(annotations)) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == RequestHeader.class) {
					RequestHeader requestHeader = (RequestHeader) annotation;
					parameterName = ObjectUtils.toString(requestHeader.value(), parameter.getName());
					headers.add(parameterName, argument != null ? (String) argument : requestHeader.defaultValue());
				}
				if (annotation.annotationType() == RequestParam.class) {
					RequestParam requestParam = (RequestParam) annotation;
					parameterName = ObjectUtils.toString(requestParam.value(), parameter.getName());
					requestParameters.put(parameterName, argument != null ? argument : requestParam.defaultValue());
				}
				if (annotation.annotationType() == PathVariable.class) {
					PathVariable pathVariable = (PathVariable) annotation;
					parameterName = ObjectUtils.toString(pathVariable.value(), parameter.getName());
					pathVariables.put(parameterName, argument);
				}
				if (annotation.annotationType() == RequestBody.class || annotation.annotationType() == ModelAttribute.class) {
					body = new HttpEntity<Object>(argument, headers);
				}
			}
		} else {
			requestParameters.put(parameter.getName(), argument);
		}

	}

	public long getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return method.name().toUpperCase() + " " + path;
	}

}
