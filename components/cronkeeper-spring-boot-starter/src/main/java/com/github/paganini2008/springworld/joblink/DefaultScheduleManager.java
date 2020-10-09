package com.github.paganini2008.springworld.joblink;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.joblink.model.JobDetail;
import com.github.paganini2008.springworld.joblink.model.JobTriggerDetail;
import com.github.paganini2008.springworld.joblink.model.TriggerDescription.Periodic;
import com.github.paganini2008.springworld.joblink.model.TriggerDescription.Team;

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
		Date startDate = triggerDetail.getStartDate();
		switch (triggerDetail.getTriggerType()) {
		case CRON:
			String cronExpression = triggerDetail.getTriggerDescriptionObject().getCron().getExpression();
			if (startDate != null) {
				return scheduler.schedule(job, attachment, cronExpression, startDate);
			}
			return scheduler.schedule(job, attachment, cronExpression);
		case PERIODIC:
			Periodic periodic = triggerDetail.getTriggerDescriptionObject().getPeriodic();
			long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
			if (startDate == null) {
				startDate = new Date(System.currentTimeMillis() + periodInMs);
			}
			if (periodic.isFixedRate()) {
				return scheduler.scheduleAtFixedRate(job, attachment, periodInMs, startDate);
			}
			return scheduler.scheduleWithFixedDelay(job, attachment, periodInMs, startDate);
		case SERIAL:
			JobKey[] dependencies = triggerDetail.getTriggerDescriptionObject().getSerial().getDependencies();
			if (startDate != null) {
				return scheduler.scheduleWithDependency(job, dependencies, startDate);
			}
			return scheduler.scheduleWithDependency(job, dependencies);
		case TEAM_CRON:
		case TEAM_PERIODIC:
		case TEAM_SERIAL:
			return doTeamSchedule(job, attachment, triggerDetail);
		default:
			break;
		}
		return null;
	}

	private JobFuture doTeamSchedule(Job job, String attachment, JobTriggerDetail triggerDetail) {
		Team team = triggerDetail.getTriggerDescriptionObject().getTeam();
		Date startDate = triggerDetail.getStartDate();
		switch (triggerDetail.getTriggerType()) {
		case TEAM_CRON:
			String cronExpression = team.getCron().getExpression();
			if (startDate != null) {
				return scheduler.schedule(job, team.getJobPeers(), cronExpression, startDate);
			}
			return scheduler.schedule(job, team.getJobPeers(), cronExpression);
		case TEAM_PERIODIC:
			Periodic periodic = team.getPeriodic();
			long periodInMs = DateUtils.convertToMillis(periodic.getPeriod(), periodic.getSchedulingUnit().getTimeUnit());
			if (startDate == null) {
				startDate = new Date(System.currentTimeMillis() + periodInMs);
			}
			if (periodic.isFixedRate()) {
				return scheduler.scheduleAtFixedRate(scheduler.createJobTeam(job, team.getJobPeers()), periodInMs, startDate);
			}
			return scheduler.scheduleWithFixedDelay(scheduler.createJobTeam(job, team.getJobPeers()), periodInMs, startDate);
		case TEAM_SERIAL:
			JobKey[] dependencies = team.getSerial().getDependencies();
			if (startDate != null) {
				return scheduler.scheduleWithDependency(scheduler.createJobTeam(job, team.getJobPeers()), dependencies, startDate);
			}
			return scheduler.scheduleWithDependency(scheduler.createJobTeam(job, team.getJobPeers()), dependencies);
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
