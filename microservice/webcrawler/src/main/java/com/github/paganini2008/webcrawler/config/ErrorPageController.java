package com.github.paganini2008.webcrawler.config;

import static com.github.paganini2008.webcrawler.Constants.REQUEST_ATTRIBUTE_START_TIME;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.webcrawler.utils.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ErrorPageController
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
@RestController
public class ErrorPageController extends AbstractErrorController {

	private static final String ERROR_PATH = "/error";

	@Autowired
	public ErrorPageController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

	@RequestMapping(value = ERROR_PATH, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Response> error(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		final Map<String, Object> body = getErrorAttributes(httpRequest, true);
		log.error("ErrorAttributes: " + body.toString());
		HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.getStatus());
		Response response = Response.failure(httpStatus.getReasonPhrase());
		response.setRequestPath(httpRequest.getServletPath()).setStatusCode(httpStatus);
		if (httpRequest.getAttribute(REQUEST_ATTRIBUTE_START_TIME) != null) {
			long startTime = (Long) httpRequest.getAttribute(REQUEST_ATTRIBUTE_START_TIME);
			response.setElapsed(System.currentTimeMillis() - startTime);
		}
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

}
