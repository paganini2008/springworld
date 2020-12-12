package com.github.paganini2008.springdessert.cluster.http;

import java.util.Map;

/**
 * 
 * ParameterizedRequest
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface ParameterizedRequest extends Request {

	Map<String, Object> getRequestParameters();

	Map<String, Object> getPathVariables();
}
