package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultScheduleManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultScheduleManager implements ScheduleManager {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobManager jobManager;

	private final Map<JobKey, JobFuture> schedulingCache = new ConcurrentHashMap<JobKey, JobFuture>();
	private final Observable trigger = Observable.unrepeatable();

	@Override
	public void schedule(final Job job) {
		trigger.addObserver((trigger, ignored) -> {
			JobKey jobKey = JobKey.of(job);
			if (hasScheduled(jobKey)) {
				log.warn("Job '{}' has been scheduled.", jobKey);
				return;
			}
			try {
				if (jobManager.hasJobState(jobKey, JobState.FINISHED)) {
					return;
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}

			JobDetail jobDetail;
			try {
				jobDetail = jobManager.getJobDetail(jobKey);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}

			final String attachment = jobDetail.getAttachment();
			schedulingCache.put(jobKey, job.getTrigger().fire(scheduler, job, attachment));

			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Schedule job '{}' ok. Current scheduling's number is {}", jobKey, countOfScheduling());
		});
	}

	@Override
	public void unscheduleJob(JobKey jobKey) {
		if (hasScheduled(jobKey)) {
			JobFuture future = schedulingCache.remove(jobKey);
			if (future != null) {
				future.cancel();
			}
			try {
				jobManager.setJobState(jobKey, JobState.NOT_SCHEDULED);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Unschedule the job: " + jobKey);
		}
	}

	@Override
	public boolean hasScheduled(JobKey jobKey) {
		return schedulingCache.containsKey(jobKey);
	}

	@Override
	public void doSchedule() {
		trigger.notifyObservers();
		log.info("Do all schedules of jobs now.");
	}

	@Override
	public int countOfScheduling() {
		return schedulingCache.size();
	}

	@Override
	public JobFuture getJobFuture(JobKey jobKey) {
		if (!hasScheduled(jobKey)) {
			throw new JobException("Not scheduling job");
		}
		return schedulingCache.get(jobKey);
	}

	@Override
	public void close() {
		for (Map.Entry<JobKey, JobFuture> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel();
		}
		schedulingCache.clear();
	}

}
