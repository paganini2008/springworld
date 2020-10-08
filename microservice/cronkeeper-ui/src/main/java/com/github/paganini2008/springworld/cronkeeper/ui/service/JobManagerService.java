package com.github.paganini2008.springworld.cronkeeper.ui.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.cronkeeper.ui.JobLogForm;
import com.github.paganini2008.springworld.cronkeeper.ui.JobTraceForm;
import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.JobManager;
import com.github.paganini2008.springworld.joblink.model.JobDetail;
import com.github.paganini2008.springworld.joblink.model.JobLog;
import com.github.paganini2008.springworld.joblink.model.JobStackTrace;
import com.github.paganini2008.springworld.joblink.model.JobTrace;
import com.github.paganini2008.springworld.joblink.model.JobTracePageQuery;
import com.github.paganini2008.springworld.joblink.model.JobTraceQuery;
import com.github.paganini2008.springworld.joblink.model.PageQuery;

/**
 * 
 * JobManagerService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Component
public class JobManagerService {

	@Autowired
	private JobManager jobManager;

	public String[] selectClusterNames() throws Exception {
		String[] clusterNames = jobManager.selectClusterNames();
		if (ArrayUtils.isEmpty(clusterNames)) {
			clusterNames = new String[] { "mycluster" };
		}
		return clusterNames;
	}

	public PageQuery<JobDetail> selectJobDetail(String clusterName, int page, int size) throws Exception {
		PageQuery<JobDetail> pageQuery = new PageQuery<JobDetail>(page, size);
		pageQuery.setClusterName(clusterName);
		jobManager.selectJobDetail(pageQuery);
		return pageQuery;
	}

	public PageQuery<JobTrace> selectJobTrace(JobTraceForm form, int page, int size) throws Exception {
		JobTracePageQuery<JobTrace> pageQuery = new JobTracePageQuery<JobTrace>(JobKey.decode(form.getJobKey()), page, size);
		pageQuery.setStartDate(form.getStartDate());
		pageQuery.setEndDate(form.getEndDate());
		jobManager.selectJobTrace(pageQuery);
		return pageQuery;
	}

	public JobDetail getJobDetail(String jobKey) throws Exception {
		return jobManager.getJobDetail(JobKey.decode(jobKey), true);
	}

	public JobLog[] selectJobLog(JobLogForm form) throws Exception {
		return jobManager.selectJobLog(new JobTraceQuery(JobKey.decode(form.getJobKey()), form.getTraceId()));
	}

	public JobStackTrace[] selectJobStackTrace(JobLogForm form) throws Exception {
		return jobManager.selectJobStackTrace(new JobTraceQuery(JobKey.decode(form.getJobKey()), form.getTraceId()));
	}

}
