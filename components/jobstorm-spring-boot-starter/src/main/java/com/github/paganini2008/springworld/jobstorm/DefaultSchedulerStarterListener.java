package com.github.paganini2008.springworld.jobstorm;

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
 * DefaultSchedulerStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultSchedulerStarterListener
		implements ApplicationListener<ApplicationClusterNewLeaderEvent>, Executable, SchedulerStarterListener {

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired(required = false)
	private JobBeanInitializer jobBeanInitializer;

	@Value("${jobstorm.scheduler.starter.refresh.inititalDelay:5}")
	private int inititalDelay;

	@Value("${jobstorm.scheduler.starter.refresh.checkInterval:60}")
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
				jobBeanInitializer.initializeJobBeans();
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
