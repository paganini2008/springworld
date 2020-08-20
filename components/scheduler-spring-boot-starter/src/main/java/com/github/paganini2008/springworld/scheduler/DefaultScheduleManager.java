package com.github.paganini2008.springworld.scheduler;

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

	@Autowired
	private JobFutureHolder jobFutureHolder;

	private final Observable jobTrigger = Observable.unrepeatable();

	@Override
	public void schedule(final Job job) {
		jobTrigger.addObserver((jobTrigger, ignored) -> {
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

			String attachment = jobDetail.getAttachment();
			TriggerType triggerType = job.getTriggerType();
			Trigger trigger = triggerType.getTrigger(job.getTriggerDescription());
			jobFutureHolder.add(jobKey, trigger.fire(scheduler, job, attachment));

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
			jobFutureHolder.cancel(jobKey);
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
		return jobFutureHolder.hasKey(jobKey);
	}

	@Override
	public void doSchedule() {
		jobTrigger.notifyObservers();
		log.info("Do all schedules of jobs now.");
	}

	@Override
	public int countOfScheduling() {
		return jobFutureHolder.size();
	}

	@Override
	public JobFuture getJobFuture(JobKey jobKey) {
		if (!hasScheduled(jobKey)) {
			throw new JobException("Not scheduling job");
		}
		return jobFutureHolder.get(jobKey);
	}

	@Override
	public void close() {
		jobFutureHolder.clear();
	}

}
