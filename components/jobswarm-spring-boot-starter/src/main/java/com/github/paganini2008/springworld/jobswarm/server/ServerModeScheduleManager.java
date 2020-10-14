package com.github.paganini2008.springworld.jobswarm.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.jobswarm.DependencyType;
import com.github.paganini2008.springworld.jobswarm.ExceptionUtils;
import com.github.paganini2008.springworld.jobswarm.Job;
import com.github.paganini2008.springworld.jobswarm.JobException;
import com.github.paganini2008.springworld.jobswarm.JobFuture;
import com.github.paganini2008.springworld.jobswarm.JobFutureHolder;
import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.JobManager;
import com.github.paganini2008.springworld.jobswarm.JobState;
import com.github.paganini2008.springworld.jobswarm.ScheduleManager;
import com.github.paganini2008.springworld.jobswarm.Scheduler;
import com.github.paganini2008.springworld.jobswarm.TriggerType;
import com.github.paganini2008.springworld.jobswarm.model.JobDetail;
import com.github.paganini2008.springworld.jobswarm.model.JobTriggerDetail;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Dependency;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Periodic;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeScheduleManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeScheduleManager implements ScheduleManager {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	private final Observable jobTrigger = Observable.unrepeatable();

	@Override
	public JobState schedule(final Job job) throws Exception {
		final JobKey jobKey = JobKey.of(job);
		jobTrigger.addObserver((jobTrigger, ignored) -> {
			if (hasScheduled(jobKey)) {
				log.warn("Job '{}' has been scheduled.", jobKey);
				return;
			}
			JobDetail jobDetail;
			try {
				if (jobManager.hasJobState(jobKey, JobState.FINISHED)) {
					return;
				}
				jobDetail = jobManager.getJobDetail(jobKey, true);
			} catch (Exception e) {
				throw ExceptionUtils.wrapExeception(e);
			}
			JobFuture jobFuture = null;
			JobTriggerDetail triggerDetail = jobDetail.getJobTriggerDetail();
			if (triggerDetail.getTriggerType() == TriggerType.DEPENDENT) {
				if (triggerDetail.getTriggerDescriptionObject().getDependency().getDependencyType() == DependencyType.SERIAL) {
					log.info("Job '{}' will be triggered on client server.", jobKey);
				} else {
					scheduleDependency(job, jobDetail.getAttachment(), triggerDetail);
				}
			} else {
				jobFuture = scheduleJob(job, jobDetail.getAttachment(), triggerDetail);
			}
			if (jobFuture != null) {
				jobFutureHolder.add(jobKey, jobFuture);
				try {
					jobManager.setJobState(jobKey, JobState.SCHEDULING);
				} catch (Exception e) {
					throw ExceptionUtils.wrapExeception(e);
				}
				log.info("Schedule job '{}' ok. Current scheduling's number is {}", jobKey, countOfScheduling());
			}

		});
		return jobManager.getJobRuntime(jobKey).getJobState();
	}

	private JobFuture scheduleJob(Job job, String attachment, JobTriggerDetail triggerDetail) {
		final TriggerDescription triggerDescription = triggerDetail.getTriggerDescriptionObject();
		Date startDate = triggerDetail.getStartDate();
		switch (triggerDetail.getTriggerType()) {
		case NONE:
			if (startDate != null) {
				return scheduler.schedule(job, attachment, startDate);
			}
			break;
		case CRON:
			String cronExpression = triggerDescription.getCron().getExpression();
			if (startDate != null) {
				return scheduler.schedule(job, attachment, cronExpression, startDate);
			}
			return scheduler.schedule(job, attachment, cronExpression);
		case PERIODIC:
			Periodic periodic = triggerDescription.getPeriodic();
			long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
			if (startDate == null) {
				startDate = new Date(System.currentTimeMillis() + periodInMs);
			}
			if (periodic.isFixedRate()) {
				return scheduler.scheduleAtFixedRate(job, attachment, periodInMs, startDate);
			}
			return scheduler.scheduleWithFixedDelay(job, attachment, periodInMs, startDate);
		default:
			break;
		}
		return null;
	}

	private JobFuture scheduleDependency(Job job, String attachment, JobTriggerDetail triggerDetail) {
		final Dependency dependency = triggerDetail.getTriggerDescriptionObject().getDependency();
		Date startDate = triggerDetail.getStartDate();
		switch (triggerDetail.getTriggerType()) {
		case NONE:
			if (startDate != null) {
				return scheduler.schedule(job, attachment, startDate);
			}
			break;
		case CRON:
			String cronExpression = dependency.getCron().getExpression();
			if (startDate != null) {
				return scheduler.schedule(job, attachment, cronExpression, startDate);
			}
			return scheduler.schedule(job, attachment, cronExpression);
		case PERIODIC:
			Periodic periodic = dependency.getPeriodic();
			long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
			if (startDate == null) {
				startDate = new Date(System.currentTimeMillis() + periodInMs);
			}
			if (periodic.isFixedRate()) {
				return scheduler.scheduleAtFixedRate(job, attachment, periodInMs, startDate);
			}
			return scheduler.scheduleWithFixedDelay(job, attachment, periodInMs, startDate);
		default:
			break;
		}
		return null;
	}

	@Override
	public JobState unscheduleJob(JobKey jobKey) throws Exception {
		if (hasScheduled(jobKey)) {
			jobFutureHolder.cancel(jobKey);
			log.info("Unschedule the job: " + jobKey);
			return jobManager.setJobState(jobKey, JobState.NOT_SCHEDULED);
		}
		return JobState.NOT_SCHEDULED;
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
