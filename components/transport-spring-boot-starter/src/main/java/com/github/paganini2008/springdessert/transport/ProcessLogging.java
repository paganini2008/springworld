package com.github.paganini2008.springdessert.transport;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.transport.buffer.BufferZone;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessLogging
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ProcessLogging implements Executable, ApplicationListener<ContextRefreshedEvent>, DisposableBean {

	@Value("${spring.application.transport.bufferzone.collectionName}")
	private String collectionName;

	@Autowired
	private Counter counter;

	@Autowired
	private BufferZone bufferZone;

	private Timer timer;

	@Override
	public boolean execute() {
		if (log.isTraceEnabled()) {
			try {
				log.trace("[Process Tip] count=" + counter.get(false) + "/" + counter.get(true) + ", tps="
						+ counter.tps(false) + "/" + counter.tps(true) + ", buffer=" + bufferZone.size(collectionName));
			} catch (Exception ignored) {
			}
		}
		return true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		timer = ThreadUtils.scheduleAtFixedRate(this, 3, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() throws Exception {
		if (timer != null) {
			timer.cancel();
		}
	}

}
