package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.ResponseEntity;

/**
 * 
 * PermissibleRequestInterceptor
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class PermissibleRequestInterceptor implements RequestInterceptor {

	@Override
	public boolean beforeSubmit(String provider, Request request) {
		return true;
	}

	@Override
	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable reason) {
		// TODO Auto-generated method stub

	}

}
