package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;

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
	private JobDependencyObservable jobDependencyObservable;

	private final Map<String, JobFuture> schedulingCache = new ConcurrentHashMap<String, JobFuture>();
	private final Observable trigger = Observable.unrepeatable();

	@Override
	public void schedule(final Job job) {
		trigger.addObserver((trigger, ignored) -> {
			if (hasScheduled(job)) {
				log.warn("Job '{}' has been scheduled.", job.getSignature());
				return;
			}
			try {
				if (jobManager.hasJobState(job, JobState.FINISHED)) {
					return;
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}

			JobDetail jobDetail;
			try {
				jobDetail = jobManager.getJobDetail(job);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			
			final String attachment = jobDetail.getAttachment();

			if (job instanceof CronJob) {
				schedulingCache.put(job.getSignature(), scheduler.schedule(job, attachment, ((CronJob) job).getCronExpression()));
			} else if (job instanceof PeriodicJob) {
				final PeriodicJob periodicJob = (PeriodicJob) job;
				long delay = DateUtils.convertToMillis(periodicJob.getDelay(), periodicJob.getDelaySchedulingUnit().getTimeUnit());
				long period = DateUtils.convertToMillis(periodicJob.getPeriod(), periodicJob.getPeriodSchedulingUnit().getTimeUnit());
				switch (periodicJob.getSchedulingMode()) {
				case FIXED_DELAY:
					schedulingCache.put(job.getSignature(), scheduler.scheduleWithFixedDelay(job, attachment, delay, period));
					break;
				case FIXED_RATE:
					schedulingCache.put(job.getSignature(), scheduler.scheduleAtFixedRate(job, attachment, delay, period));
					break;
				}
			} else if (job instanceof SerialJob) {
				SerialJob serialJob = (SerialJob) job;
				schedulingCache.put(serialJob.getSignature(), JobFuture.EMPTY);
				jobDependencyObservable.addDependency(serialJob);
			} else {
				throw new JobException("Only support for CronJob, PeriodicJob and SerialJob.");
			}

			try {
				jobManager.setJobState(job, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Schedule job '{}' ok. Current scheduling's number is {}", job.getSignature(), countOfScheduling());
		});
	}

	@Override
	public void unscheduleJob(Job job) {
		if (hasScheduled(job)) {
			JobFuture future = schedulingCache.remove(job.getSignature());
			if (future != null) {
				future.cancel();
			}
			try {
				jobManager.setJobState(job, JobState.NOT_SCHEDULED);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Unschedule the job: " + job.getSignature());
		}
	}

	@Override
	public boolean hasScheduled(Job job) {
		return schedulingCache.containsKey(job.getSignature());
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
	public JobFuture getFuture(Job job) {
		if (!hasScheduled(job)) {
			throw new JobException("Not scheduling job");
		}
		return schedulingCache.get(job.getSignature());
	}

	@Override
	public void close() {
		for (Map.Entry<String, JobFuture> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel();
		}
		schedulingCache.clear();
	}

}
