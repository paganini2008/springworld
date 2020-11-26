package com.github.paganini2008.springdessert.jobsoup.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.paganini2008.devtools.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * GlobalExceptionalHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionalHandler {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<UIModel<?>> handleException(HttpServletRequest request, Exception e) throws Exception {
		log.error(e.getMessage(), e);
		UIModel<?> result = UIModel.failure("Internal Server Error");
		result.setRequestPath(request.getServletPath());
		result.setErrorMsg(e.getMessage());
		result.setErrorDetail(ExceptionUtils.toArray(e));
		if (request.getAttribute("sign") != null) {
			long sign = (Long) request.getAttribute("sign");
			result.setElapsed(System.currentTimeMillis() - sign);
		}
		return ResponseEntity.ok(result);
	}

}