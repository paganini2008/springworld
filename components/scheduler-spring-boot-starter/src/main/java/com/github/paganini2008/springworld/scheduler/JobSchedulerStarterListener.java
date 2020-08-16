package com.github.paganini2008.springworld.scheduler;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobSchedulerStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobSchedulerStarterListener implements ApplicationListener<ApplicationClusterNewLeaderEvent>, Executable {

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired(required = false)
	private NotManagedJobBeanInitializer jobBeanInitializer;

	@Value("${spring.application.cluster.scheduler.launcher.inititalDelay:5}")
	private int inititalDelay;

	@Value("${spring.application.cluster.scheduler.launcher.checkInterval:60}")
	private int checkInterval;

	private Timer timer;

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		timer = ThreadUtils.scheduleWithFixedDelay(this, inititalDelay, checkInterval, TimeUnit.SECONDS);
	}

	@Override
	public boolean execute() {
		if (jobBeanInitializer != null) {
			try {
				jobBeanInitializer.initizlizeJobBeans();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		scheduleManager.doSchedule();
		return jobBeanInitializer != null;
	}

	@PreDestroy
	public void stop() {
		if (timer != null) {
			timer.cancel();
		}
	}

}