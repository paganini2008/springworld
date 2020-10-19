package com.github.paganini2008.springworld.jobswarm;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.jobswarm.model.JobParam;
import com.github.paganini2008.springworld.jobswarm.model.JobPeerResult;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialDependencySchedulerImpl
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SerialDependencySchedulerImpl extends Observable implements SerialDependencyScheduler {

	public SerialDependencySchedulerImpl() {
		super(Boolean.TRUE);
	}

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobFutureHolder JobFutureHolder;

	@Override
	public JobFuture scheduleDependency(final Job job, final JobKey... dependencies) {
		List<Observer> obs = new CopyOnWriteArrayList<Observer>();
		for (final JobKey dependency : dependencies) {
			Observer ob = (o, attachment) -> {
				final JobPeerResult jobPeerResult = (JobPeerResult) attachment;
				if (((JobDependency) job).approve(dependency, jobPeerResult.getRunningState(), jobPeerResult.getAttachment(),
						jobPeerResult.getResult())) {
					jobExecutor.execute(job, jobPeerResult.getResult(), 0);
				}
			};
			addObserver(dependency.getIdentifier(), ob);
			obs.add(ob);
			if (log.isTraceEnabled()) {
				log.trace("Add job dependency '{}' to job '{}'", dependency, job);
			}
		}
		return new SerialDependencyFuture(new CopyOnWriteArrayList<JobKey>(Arrays.asList(dependencies)), obs, this);
	}

	@Override
	public void updateDependency(final Job job, final JobKey... dependencies) {
		final JobKey jobKey = JobKey.of(job);
		JobFuture jobFuture = JobFutureHolder.get(jobKey);
		if (!(jobFuture instanceof SerialDependencyFuture)) {
			throw new JobException("Job '" + jobKey + "' is not a serial dependentcy job.");
		}
		SerialDependencyFuture jobDependencyFuture = (SerialDependencyFuture) jobFuture;
		for (JobKey dependency : dependencies) {
			if (jobDependencyFuture.getDependencies().contains(dependency)) {
				continue;
			}
			Observer ob = (o, attachment) -> {
				final JobPeerResult jobPeerResult = (JobPeerResult) attachment;
				if (((JobDependency) job).approve(dependency, jobPeerResult.getRunningState(), jobPeerResult.getAttachment(),
						jobPeerResult.getResult())) {
					jobExecutor.execute(job, jobPeerResult.getResult(), 0);
				}
			};
			addObserver(dependency.getIdentifier(), ob);
			
			jobDependencyFuture.getDependencies().add(dependency);
			jobDependencyFuture.getObservers().add(ob);
			if (log.isTraceEnabled()) {
				log.trace("Update job dependency '{}' to job '{}'", dependency, job);
			}
		}
	}

	@Override
	public boolean hasScheduled(JobKey jobKey) {
		return JobFutureHolder.get(jobKey) instanceof SerialDependencyFuture;
	}

	@Override
	public void triggerDependency(JobKey jobKey, Object attachment) {
		notifyObservers(jobKey.getIdentifier(), attachment);
	}

	@Override
	public void notifyDependants(JobKey jobKey, Object attachment) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ jobKey.getIdentifier();
		JobParam jobParam = new JobParam(jobKey, attachment, 0);
		redisMessageSender.sendMessage(channel, jobParam);
		if (log.isTraceEnabled()) {
			log.trace("Immediately job '{}' is done and all serial dependencies will be notfied.", jobKey);
		}
	}

}
