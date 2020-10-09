package com.github.paganini2008.springworld.joblink;

import com.github.paganini2008.springworld.joblink.model.JobDetail;
import com.github.paganini2008.springworld.joblink.model.JobLog;
import com.github.paganini2008.springworld.joblink.model.JobKeyQuery;
import com.github.paganini2008.springworld.joblink.model.JobRuntime;
import com.github.paganini2008.springworld.joblink.model.JobStackTrace;
import com.github.paganini2008.springworld.joblink.model.JobTrace;
import com.github.paganini2008.springworld.joblink.model.JobTracePageQuery;
import com.github.paganini2008.springworld.joblink.model.JobTraceQuery;
import com.github.paganini2008.springworld.joblink.model.JobTriggerDetail;
import com.github.paganini2008.springworld.joblink.model.PageQuery;

/**
 * 
 * JobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobManager extends JobPersistence, LifeCycle {

	String[] selectClusterNames() throws Exception;

	JobState pauseJob(JobKey jobKey) throws Exception;

	JobState resumeJob(JobKey jobKey) throws Exception;

	boolean hasJobState(JobKey jobKey, JobState jobState) throws Exception;

	JobState setJobState(JobKey jobKey, JobState jobState) throws Exception;

	JobDetail getJobDetail(JobKey jobKey, boolean detailed) throws Exception;

	JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws Exception;

	boolean hasRelations(JobKey jobKey) throws Exception;

	JobKey[] getDependencies(JobKey jobKey) throws Exception;

	JobKey[] getJobKeys(JobKeyQuery jobQuery) throws Exception;

	int getJobId(JobKey jobKey) throws Exception;

	JobRuntime getJobRuntime(JobKey jobKey) throws Exception;

	void selectJobDetail(PageQuery<JobDetail> pageQuery) throws Exception;

	void selectJobTrace(JobTracePageQuery<JobTrace> pageQuery) throws Exception;

	JobStackTrace[] selectJobStackTrace(JobTraceQuery query) throws Exception;

	JobLog[] selectJobLog(JobTraceQuery query) throws Exception;

}
