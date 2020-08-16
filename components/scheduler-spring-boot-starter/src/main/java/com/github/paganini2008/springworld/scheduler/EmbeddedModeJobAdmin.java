package com.github.paganini2008.springworld.scheduler;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class EmbeddedModeJobAdmin implements ClusterMessageListener, JobAdmin {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	public void addJob(JobConfig jobConfig) {
		clusterMulticastGroup.unicast(jobConfig.getGroupName(), getTopic(), jobConfig);
	}

	public void deleteJob(JobKey jobKey) {
		try {
			jobManager.deleteJob(jobKey);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	public boolean hasJob(JobKey jobKey) {
		try {
			return jobManager.hasJob(jobKey);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		final JobConfig jobConfig = (JobConfig) message;
		Job job = new JobAddRequest(jobConfig);
		try {
			jobManager.addJob(job, jobConfig.getAttachment());
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String getTopic() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:add";
	}

}
