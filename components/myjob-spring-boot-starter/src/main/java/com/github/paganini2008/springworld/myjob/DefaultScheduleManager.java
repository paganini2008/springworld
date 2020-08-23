package com.github.paganini2008.springworld.myjob;

import java.sql.SQLException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.myjob.TriggerDescription.Periodic;

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
			} catch (SQLException e) {
				throw new JobException(e.getMessage(), e);
			}

			JobDetail jobDetail;
			JobTriggerDetail triggerDetail;
			try {
				jobDetail = jobManager.getJobDetail(jobKey);
				triggerDetail = jobManager.getJobTriggerDetail(jobKey);
			} catch (SQLException e) {
				throw new JobException(e.getMessage(), e);
			}

			String attachment = jobDetail.getAttachment();
			jobFutureHolder.add(jobKey, doSchedule(job, attachment, triggerDetail));

			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Schedule job '{}' ok. Current scheduling's number is {}", jobKey, countOfScheduling());
		});
	}

	private JobFuture doSchedule(Job job, String attachment, JobTriggerDetail triggerDetail) {
		Date startDate = triggerDetail.getStartDate();
		switch (triggerDetail.getTriggerType()) {
		case CRON:
			String cronExpression = triggerDetail.getTriggerDescription().getCron().getExpression();
			if (startDate != null) {
				return scheduler.schedule(job, attachment, cronExpression, startDate);
			}
			return scheduler.schedule(job, attachment, cronExpression);
		case PERIODIC:
			Periodic periodic = triggerDetail.getTriggerDescription().getPeriodic();
			long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
			if (startDate == null) {
				startDate = new Date(System.currentTimeMillis() + periodInMs);
			}
			if (periodic.isFixedRate()) {
				return scheduler.scheduleAtFixedRate(job, attachment, periodInMs, startDate);
			}
			return scheduler.scheduleWithFixedDelay(job, attachment, periodInMs, startDate);
		case SERIAL:
			String[] dependencies = triggerDetail.getTriggerDescription().getSerial().getDependencies();
			if (startDate != null) {
				return scheduler.scheduleWithDependency(job, dependencies, startDate);
			}
			return scheduler.scheduleWithDependency(job, dependencies);
		}
		throw new IllegalStateException();
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
