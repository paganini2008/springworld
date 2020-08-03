package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * ScheduleManagerController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/schedule/manager")
public class ScheduleManagerController {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@PostMapping("/runJob")
	public ResponseEntity<String> runJob(@RequestBody JobParameter jobParameter) throws Exception {
		Job job = jobBeanLoader.defineJob(jobParameter);
		jobExecutor.execute(job, jobParameter.getArgument());
		return ResponseEntity.ok("ok");
	}

}
