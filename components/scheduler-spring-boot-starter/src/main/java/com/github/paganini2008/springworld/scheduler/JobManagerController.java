package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * JobManagerController
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/job")
public class JobManagerController {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private JobManager jobManager;

	@GetMapping("/stat")
	public ResponseEntity<JobInfo> jobStat(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "PAGE_SIZE", required = false, defaultValue = "10") int size) {
		return null;
	}

}
