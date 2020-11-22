package com.github.paganini2008.springworld.jobsoup;

import com.github.paganini2008.springworld.jobsoup.model.JobDetail;
import com.github.paganini2008.springworld.jobsoup.model.JobKeyQuery;
import com.github.paganini2008.springworld.jobsoup.model.JobLog;
import com.github.paganini2008.springworld.jobsoup.model.JobRuntime;
import com.github.paganini2008.springworld.jobsoup.model.JobStackTrace;
import com.github.paganini2008.springworld.jobsoup.model.JobTrace;
import com.github.paganini2008.springworld.jobsoup.model.JobTracePageQuery;
import com.github.paganini2008.springworld.jobsoup.model.JobTraceQuery;
import com.github.paganini2008.springworld.jobsoup.model.JobTriggerDetail;
import com.github.paganini2008.springworld.jobsoup.model.PageQuery;

/**
 * 
 * JobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobManager extends JobPersistence {

	String[] selectClusterNames() throws Exception;

	JobDetail getJobDetail(JobKey jobKey, boolean detailed) throws Exception;

	JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws Exception;

	boolean hasRelations(JobKey jobKey, DependencyType dependencyType) throws Exception;

	JobKey[] getRelations(JobKey jobKey, DependencyType dependencyType) throws Exception;

	JobKey[] getDependentKeys(JobKey jobKey, DependencyType dependencyType) throws Exception;

	JobKey[] getJobKeys(JobKeyQuery jobQuery) throws Exception;

	int getJobId(JobKey jobKey) throws Exception;

	JobRuntime getJobRuntime(JobKey jobKey) throws Exception;

	void selectJobDetail(PageQuery<JobDetail> pageQuery) throws Exception;

	void selectJobTrace(JobTracePageQuery<JobTrace> pageQuery) throws Exception;

	JobStackTrace[] selectJobStackTrace(JobTraceQuery query) throws Exception;

	JobLog[] selectJobLog(JobTraceQuery query) throws Exception;

}
