package com.github.paganini2008.springworld.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultJobDependencyObservable
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultJobDependencyObservable extends Observable implements JobDependencyObservable {

	public DefaultJobDependencyObservable() {
		super(true);
	}

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("main-job-executor")
	@Autowired
	private JobExecutor jobExecutor;

	private final Map<JobKey, JobFuture> jobFutures = new ConcurrentHashMap<JobKey, JobFuture>();

	@Override
	public JobFuture addDependency(final Job job, final String[] dependencies) {
		List<Observer> obs = new ArrayList<Observer>();
		for (String dependency : dependencies) {
			Observer ob = (o, attachment) -> {
				jobExecutor.execute(job, attachment);
			};

			addObserver(dependency, ob);

			obs.add(ob);
			if (log.isTraceEnabled()) {
				log.trace("Job dependency: {} --> {}", dependency, job);
			}
		}
		final JobKey jobKey = JobKey.of(job);
		jobFutures.put(jobKey, new JobDependencyFuture(obs.toArray(new Observer[0]), dependencies, this));
		return jobFutures.get(jobKey);
	}

	@Override
	public void cancelDependency(JobKey jobKey) {
		if (hasDependency(jobKey)) {
			jobFutures.remove(jobKey).cancel();
		}
	}

	@Override
	public boolean hasDependency(JobKey jobKey) {
		return jobFutures.containsKey(jobKey);
	}

	@Override
	public void executeDependency(JobKey jobKey, Object attachment) {
		notifyObservers(jobKey.getIdentifier(), attachment);
		if (log.isTraceEnabled()) {
			log.trace("Job '{}' has done and start to execute other dependent job.", jobKey);
		}
	}

	@Override
	public void notifyDependants(JobKey jobKey, Object result) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ jobKey.getIdentifier();
		JobParam jobParam = new JobParam(jobKey, result);
		redisMessageSender.sendMessage(channel, jobParam);
		if (log.isTraceEnabled()) {
			log.trace("Notify other dependants after job '{}' has done", jobKey);
		}
	}

}
