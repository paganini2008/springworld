package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.jobclick.model.JobDetail;
import com.github.paganini2008.springworld.jobclick.model.JobTriggerDetail;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription.Milestone;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription.Periodic;

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
				throw new JobException(e.getMessage(), e);
			}

			JobFuture jobFuture = doSchedule(job, jobDetail.getAttachment(), jobDetail.getJobTriggerDetail());
			if (jobFuture != null) {
				jobFutureHolder.add(jobKey, jobFuture);
			}
			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Schedule job '{}' ok. Current scheduling's number is {}", jobKey, countOfScheduling());
		});
		return jobManager.getJobRuntime(jobKey).getJobState();
	}

	private JobFuture doSchedule(Job job, String attachment, JobTriggerDetail triggerDetail) {
		TriggerDescription triggerDescription = triggerDetail.getTriggerDescriptionObject();
		if (triggerDescription.getMilestone() != null) {
			Milestone milestone = triggerDescription.getMilestone();
			Date startDate = triggerDetail.getStartDate();
			switch (triggerDetail.getTriggerType()) {
			case CRON:
				String cronExpression = triggerDescription.getCron().getExpression();
				if (startDate != null) {
					return scheduler.schedule(scheduler.createJobTeam(job, milestone.getCooperators()), cronExpression, startDate);
				}
				return scheduler.schedule(scheduler.createJobTeam(job, milestone.getCooperators()), cronExpression);
			case PERIODIC:
				Periodic periodic = triggerDescription.getPeriodic();
				long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
				if (startDate == null) {
					startDate = new Date(System.currentTimeMillis() + periodInMs);
				}
				if (periodic.isFixedRate()) {
					return scheduler.scheduleAtFixedRate(scheduler.createJobTeam(job, milestone.getCooperators()), periodInMs, startDate);
				}
				return scheduler.scheduleWithFixedDelay(scheduler.createJobTeam(job, milestone.getCooperators()), periodInMs, startDate);
			case SERIAL:
				JobKey[] dependencies = triggerDescription.getSerial().getDependencies();
				if (startDate != null) {
					return scheduler.scheduleWithDependency(scheduler.createJobTeam(job, milestone.getCooperators()), dependencies,
							startDate);
				}
				return scheduler.scheduleWithDependency(scheduler.createJobTeam(job, milestone.getCooperators()), dependencies);
			default:
				break;
			}
		} else {
			Date startDate = triggerDetail.getStartDate();
			switch (triggerDetail.getTriggerType()) {
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
			case SERIAL:
				JobKey[] dependencies = triggerDescription.getSerial().getDependencies();
				if (startDate != null) {
					return scheduler.scheduleWithDependency(job, dependencies, startDate);
				}
				return scheduler.scheduleWithDependency(job, dependencies);
			default:
				break;
			}
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
