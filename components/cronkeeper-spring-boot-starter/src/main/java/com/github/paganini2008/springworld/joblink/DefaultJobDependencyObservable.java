package com.github.paganini2008.springworld.joblink;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.joblink.model.JobParam;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

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
		super(Boolean.TRUE);
	}

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public JobFuture addDependency(final Job job, final JobKey... dependencies) {
		List<Observer> obs = new ArrayList<Observer>(dependencies.length);
		for (JobKey dependency : dependencies) {
			Observer ob = (o, attachment) -> {
				jobExecutor.execute(job, attachment, 0);
			};
			addObserver(dependency.getIdentifier(), ob);
			obs.add(ob);

			if (log.isTraceEnabled()) {
				log.trace("Job dependency: {} --> {}", dependency, job);
			}
		}
		return new JobDependencyFuture(dependencies, obs.toArray(new Observer[0]), this);
	}

	@Override
	public JobFuture addDependency(final JobTeam jobTeam, final JobKey... dependencies) {
		List<Observer> obs = new ArrayList<Observer>(dependencies.length);
		for (JobKey dependency : dependencies) {
			Observer ob = (o, attachment) -> {
				jobTeam.cooperate(attachment);
			};
			addObserver(dependency.getIdentifier(), ob);
			obs.add(ob);

			if (log.isTraceEnabled()) {
				log.trace("Job dependency: {} --> {}", dependency, jobTeam);
			}
		}
		return new JobDependencyFuture(dependencies, obs.toArray(new Observer[0]), this);
	}

	@Override
	public void executeDependency(JobKey jobKey, Object attachment) {
		notifyObservers(jobKey.getIdentifier(), attachment);
		if (log.isTraceEnabled()) {
			log.trace("Job '{}' has done and start to execute other dependent job.", jobKey);
		}
	}

	@Override
	public void notifyDependants(JobKey jobKey, Object attachment) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ jobKey.getIdentifier();
		JobParam jobParam = new JobParam(jobKey, attachment, 0);
		redisMessageSender.sendMessage(channel, jobParam);
		if (log.isTraceEnabled()) {
			log.trace("Notify other dependants after job '{}' has done", jobKey);
		}
	}

}
