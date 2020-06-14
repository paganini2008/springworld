package com.github.paganini2008.springworld.cluster.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MemoryJobManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class MemoryJobManager implements JobManager {

	private final Observable observable = Observable.unrepeatable();
	private final Map<Job, JobBeanProxy> jobBeanHolder = new ConcurrentHashMap<Job, JobBeanProxy>();
	private final Map<Job, ScheduledFuture<?>> schedulingCache = new ConcurrentHashMap<Job, ScheduledFuture<?>>();

	@Autowired
	private TaskScheduler taskScheduler;

	@Value("${spring.cluster.scheduler.loadbalance.enabled:true}")
	private boolean loadbalanced;

	@Override
	public void addJob(Job job) {
		checkJobNameIfBlank(job);
		jobBeanHolder.putIfAbsent(job, getJobBean(job));
	}

	@Override
	public void deleteJob(Job job) {
		jobBeanHolder.remove(job);
	}

	@Override
	public boolean hasJob(Job job) {
		return jobBeanHolder.containsKey(job);
	}

	@Override
	public void schedule(final Job job) {
		checkJobNameIfBlank(job);
		observable.addObserver((ob, arg) -> {
			if (!hasScheduled(job)) {
				if (job instanceof CronJob) {
					CronJob cronJob = (CronJob) job;
					schedulingCache.put(job,
							taskScheduler.schedule((Runnable) jobBeanHolder.get(job), new CronTrigger(cronJob.getCronExpression())));
				} else if (job instanceof ScheduledJob) {
					ScheduledJob scheduledJob = (ScheduledJob) job;
					long delay = DateUtils.convertToMillis(scheduledJob.getDelay(), scheduledJob.getDelayUnit());
					long period = DateUtils.convertToMillis(scheduledJob.getPeriod(), scheduledJob.getPeriodUnit());
					Date startDate = new Date(System.currentTimeMillis() + delay);
					Runnable jobBean = (Runnable) jobBeanHolder.get(job);
					switch (scheduledJob.getRunningMode()) {
					case FIXED_DELAY:
						taskScheduler.scheduleWithFixedDelay(jobBean, startDate, period);
						break;
					case FIXED_RATE:
						taskScheduler.scheduleAtFixedRate(jobBean, startDate, period);
						break;
					default:
						throw new IllegalStateException();
					}
				} else {
					throw new SchedulingException("Please implement the job interface for CronJob or ScheduledJob.");
				}
				log.info("Schedule job '" + job.getName() + "@" + job.getClass().getName() + "' ok. Currently scheduling's size is "
						+ countOfScheduling());
			}
		});
	}

	protected JobBeanProxy getJobBean(Job job) {
		JobBeanProxy bean = loadbalanced ? new LoadBalancedJobBeanProxy(job) : new DefaultJobBeanProxy(job);
		return ApplicationContextUtils.autowireBean(bean);
	}

	@Override
	public void unscheduleJob(Job job) {
		if (hasScheduled(job)) {
			ScheduledFuture<?> scheduledFuture = schedulingCache.remove(job);
			if (scheduledFuture != null) {
				scheduledFuture.cancel(false);
			}
			log.info("Unschedule job: " + job.getName() + "@" + job.getClass().getName());
		}
	}

	@Override
	public void pauseJob(Job job) {
		if (hasScheduled(job)) {
			jobBeanHolder.get(job).pause();
		}
	}

	@Override
	public void resumeJob(Job job) {
		if (hasScheduled(job)) {
			jobBeanHolder.get(job).resume();
		}
	}

	@Override
	public boolean hasScheduled(Job job) {
		return jobBeanHolder.containsKey(job) && schedulingCache.containsKey(job);
	}

	@Override
	public void testJob(Job job) {
		((Runnable) getJobBean(job)).run();
	}

	@Override
	public void runNow() {
		observable.notifyObservers();
		log.info("Run all jobs now.");
	}

	@Override
	public int countOfScheduling() {
		return schedulingCache.size();
	}

	private static void checkJobNameIfBlank(Job job) {
		if (StringUtils.isBlank(job.getName())) {
			throw new SchedulingException("Job name is not blank for class: " + job.getClass().getName());
		}
	}

	@Override
	public void close() {
		for (Map.Entry<Job, ScheduledFuture<?>> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel(false);
		}
		schedulingCache.clear();
		jobBeanHolder.clear();
	}
}
