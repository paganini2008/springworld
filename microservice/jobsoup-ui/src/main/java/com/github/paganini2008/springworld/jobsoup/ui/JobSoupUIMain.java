package com.github.paganini2008.springworld.jobsoup.ui;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.paganini2008.devtools.io.FileUtils;

/**
 * 
 * JobSoupUIMain
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.github.paganini2008.springworld.jobstorm.ui" })
public class JobSoupUIMain {

	static {
		System.setProperty("spring.devtools.restart.enabled", "false");
		File logDir = FileUtils.getFile(FileUtils.getUserDirectory(), "logs", "springworld", "jobstorm", "ui");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		System.setProperty("DEFAULT_LOG_BASE", logDir.getAbsolutePath());
	}

	public static void main(String[] args) {
		SpringApplication.run(JobSoupUIMain.class, args);
		System.out.println(Env.getPid());
	}

}
