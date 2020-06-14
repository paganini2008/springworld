package com.github.paganini2008.springworld.cluster.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoadBalancedJobBeanProxy
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class LoadBalancedJobBeanProxy implements Runnable, JobBeanProxy {

	private final Job job;
	private final AtomicBoolean running = new AtomicBoolean(true);

	LoadBalancedJobBeanProxy(Job job) {
		this.job = job;
	}

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public void run() {
		if (!isRunning()) {
			return;
		}
		final String sending = job.getName() + "@" + job.getJobClassName();
		final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + ":scheduler:" + sending;
		clusterMulticastGroup.unicast(topic, sending);
	}

	public boolean isRunning() {
		return running.get();
	}

	public void pause() {
		running.set(false);
		if (log.isTraceEnabled()) {
			log.trace("Pause job: " + job.getName() + "/" + job.getJobClassName());
		}
	}

	public void resume() {
		running.set(true);
		if (log.isTraceEnabled()) {
			log.trace("Resume job: " + job.getName() + "/" + job.getJobClassName());
		}
	}

}
