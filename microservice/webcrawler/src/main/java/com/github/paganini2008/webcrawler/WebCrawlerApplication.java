package com.github.paganini2008.webcrawler;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.springdessert.jdbc.annotations.DaoScan;

/**
 * 
 * WebCrawlerApplication
 *
 * @author Fred Feng
 * @since 1.0
 */
@DaoScan(basePackages = "com.github.paganini2008.springdessert.webcrawler.jdbc")
@SpringBootApplication
@ComponentScan(basePackages = { "com.github.paganini2008.webcrawler"})
public class WebCrawlerApplication {

	static {
		System.setProperty("spring.devtools.restart.enabled", "false");
		File logDir = FileUtils.getFile(FileUtils.getUserDirectory(), "logs", "springworld", "examples");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		System.setProperty("DEFAULT_LOG_BASE", logDir.getAbsolutePath());
	}

	public static void main(String[] args) {
		// final int port =
		// NetUtils.getRandomPort(Constants.MICROSERVICE_RANDOM_PORT_START,
		// Constants.MICROSERVICE_BIZ_RANDOM_PORT_END);
		int port = 8021;
		System.out.println("Server Port: " + port);
		System.setProperty("server.port", String.valueOf(port));
		SpringApplication.run(WebCrawlerApplication.class, args);
		System.out.println(Env.getPid());
	}
}
