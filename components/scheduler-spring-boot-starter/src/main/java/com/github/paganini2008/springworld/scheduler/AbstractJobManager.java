package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * AbstractJobManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public abstract class AbstractJobManager implements JobManager {

	@Autowired
	private Scheduler scheduler;

	private final Map<Job, Future> schedulingCache = new ConcurrentHashMap<Job, Future>();
	private final Observable trigger = Observable.unrepeatable();
	private final Observable dependencies = Observable.repeatable();

	@Override
	public void schedule(final Job job, final Object arg) {
		trigger.addObserver((trigger, ignored) -> {
			if (!hasScheduled(job)) {
				if (job instanceof CronJob) {
					schedulingCache.put(job, scheduler.schedule(job, arg, ((CronJob) job).getCronExpression()));
				} else if (job instanceof PeriodicJob) {
					final PeriodicJob periodicJob = (PeriodicJob) job;
					long delay = DateUtils.convertToMillis(periodicJob.getDelay(), periodicJob.getDelayTimeUnit());
					long period = DateUtils.convertToMillis(periodicJob.getPeriod(), periodicJob.getPeriodTimeUnit());
					switch (periodicJob.getRunningMode()) {
					case FIXED_DELAY:
						schedulingCache.put(job, scheduler.scheduleWithFixedDelay(job, arg, delay, period));
						break;
					case FIXED_RATE:
						schedulingCache.put(job, scheduler.scheduleAtFixedRate(job, arg, delay, period));
						break;
					}
				} else if (job instanceof SerializableJob) {
					addJobDependency((SerializableJob) job);
				} else {
					throw new JobException("Please implement the job interface for CronJob or ScheduledJob.");
				}

				setJobState(job, JobState.SCHEDULING);
				log.info("Schedule job '" + job.getSignature() + "' ok. Currently scheduling's size is " + countOfScheduling());
			}
		});
	}

	@Override
	public void unscheduleJob(Job job) {
		if (hasScheduled(job)) {
			Future future = schedulingCache.remove(job);
			if (future != null) {
				future.cancel();
			}
			log.info("Unschedule job: " + job.getSignature());
		}
	}

	@Override
	public boolean hasScheduled(Job job) {
		return hasJob(job) && schedulingCache.containsKey(job);
	}

	@Override
	public void runJob(Job job, Object arg) {
		scheduler.runJob(job, arg);
	}

	@Override
	public void pauseJob(Job job) throws Exception {
		if (hasScheduled(job)) {
			setJobState(job, JobState.PAUSED);
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + job.getSignature());
			}
		}
	}

	@Override
	public void resumeJob(Job job) throws Exception {
		if (hasScheduled(job)) {
			setJobState(job, JobState.RUNNING);
			if (log.isTraceEnabled()) {
				log.trace("Resume the job: " + job.getSignature());
			}
		}
	}

	@Override
	public void doSchedule() {
		trigger.notifyObservers();
		log.info("Run all jobs now.");
	}

	@Override
	public int countOfScheduling() {
		return schedulingCache.size();
	}

	@Override
	public void addJobDependency(SerializableJob job) {
		for (String signature : job.getDependencies()) {
			dependencies.addObserver(signature, (ob, arg) -> {
				scheduler.runJob(job, arg);
			});
		}
	}

	@Override
	public Future getFuture(Job job) {
		if (!schedulingCache.containsKey(job)) {
			throw new JobException("Not scheduling");
		}
		return schedulingCache.get(job);
	}

	@Override
	public void close() {
		for (Map.Entry<Job, Future> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel();
		}
		schedulingCache.clear();
	}

	protected abstract void setJobState(Job job, JobState jobState);

}
