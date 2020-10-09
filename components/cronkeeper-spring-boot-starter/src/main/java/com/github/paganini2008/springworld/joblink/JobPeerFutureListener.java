package com.github.paganini2008.springworld.joblink;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.joblink.model.JobKeyQuery;
import com.github.paganini2008.springworld.joblink.model.JobPeer;
import com.github.paganini2008.springworld.joblink.model.JobTriggerDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobPeerFutureListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobPeerFutureListener implements ApplicationListener<ApplicationClusterNewLeaderEvent>, Executable, LifeCycle {

	private Timer timer;

	@Autowired
	private JobManager jobManager;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@PreDestroy
	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public boolean execute() {
		refresh();
		return true;
	}

	private void refresh() {
		final TriggerType[] triggerTypes = new TriggerType[] { TriggerType.TEAM_CRON, TriggerType.TEAM_PERIODIC, TriggerType.TEAM_SERIAL };
		for (TriggerType triggerType : triggerTypes) {
			refresh(triggerType);
		}
	}

	private void refresh(TriggerType triggerType) {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(triggerType);
		JobKey[] jobKeys = new JobKey[0];
		try {
			jobKeys = jobManager.getJobKeys(jobQuery);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		Map<JobKey, JobKey[]> allDependencies = new HashMap<JobKey, JobKey[]>();
		JobKey[] dependencies;
		JobKey[] comparedDependencies;
		JobKey[] supplyJobKeys;
		for (JobKey jobKey : jobKeys) {
			JobTriggerDetail triggerDetail = null;
			try {
				triggerDetail = jobManager.getJobTriggerDetail(jobKey);
				JobPeer[] jobPeers = triggerDetail.getTriggerDescriptionObject().getTeam().getJobPeers();
				dependencies = new JobKey[jobPeers.length];
				for (int i = 0; i < dependencies.length; i++) {
					dependencies[i] = jobPeers[i].getJobKey();
				}
				comparedDependencies = jobManager.getDependencies(jobKey);
				supplyJobKeys = ArrayUtils.minus(dependencies, comparedDependencies);
				if (ArrayUtils.isNotEmpty(supplyJobKeys)) {
					allDependencies.put(jobKey, supplyJobKeys);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		for (Map.Entry<JobKey, JobKey[]> entry : allDependencies.entrySet()) {
			JobKey jobKey = entry.getKey();
			for (JobKey dependency : entry.getValue()) {
				try {
					if (jobManager.hasJob(dependency)) {
						saveJobDependency(jobKey, dependency);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	private void saveJobDependency(JobKey jobKey, JobKey dependency) {
		Connection connection = null;
		try {
			final int jobId = jobManager.getJobId(jobKey);
			final int dependentId = jobManager.getJobId(dependency);
			DataSource dataSource = ApplicationContextUtils.getBean(BeanNames.DATA_SOURCE, DataSource.class);
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_DEPENDENCY, ps -> {
				ps.setInt(1, jobId);
				ps.setInt(2, dependentId);
			});
			log.info("Add job dependency '{}' to jobId {} ok.", dependency, jobId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		this.timer = ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.MINUTES);
	}
}
