package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * HealthCheckJob
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Component
public class HealthCheckJob implements CronJob {

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public Object execute(Object arg) {
		log.trace(info());
		return null;
	}

	@Override
	public String getGroupName() {
		return applicationName;
	}

	@Override
	public String getCronExpression() {
		return "*/5 * * * * ?";
	}

	private String info() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
		return FileUtils.formatSize(usedMemory) + "/" + FileUtils.formatSize(totalMemory);
	}

}
