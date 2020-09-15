package com.github.paganini2008.springworld.cronkeeper;

import java.sql.SQLException;

import com.github.paganini2008.springworld.cronkeeper.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.model.JobLog;
import com.github.paganini2008.springworld.cronkeeper.model.JobQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobRuntime;
import com.github.paganini2008.springworld.cronkeeper.model.JobStackTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobTracePageQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTraceQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTriggerDetail;
import com.github.paganini2008.springworld.cronkeeper.model.PageQuery;

/**
 * 
 * JobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobManager extends JobPersistence, Lifecycle {

	JobState pauseJob(JobKey jobKey) throws Exception;

	JobState resumeJob(JobKey jobKey) throws Exception;

	boolean hasJobState(JobKey jobKey, JobState jobState) throws Exception;

	JobState setJobState(JobKey jobKey, JobState jobState) throws Exception;

	JobDetail getJobDetail(JobKey jobKey, boolean detailed) throws Exception;

	JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws Exception;

	boolean hasRelations(JobKey jobKey) throws Exception;

	JobKey[] getDependencies(JobKey jobKey) throws Exception;

	JobKey[] getJobKeys(JobQuery jobQuery) throws Exception;

	int getJobId(JobKey jobKey) throws Exception;

	JobRuntime getJobRuntime(JobKey jobKey) throws Exception;

	void selectJobDetail(PageQuery<JobDetail> pageQuery) throws Exception;

	void selectJobTrace(JobTracePageQuery<JobTrace> pageQuery) throws Exception;

	JobStackTrace[] selectJobStackTrace(JobTraceQuery query) throws SQLException;

	JobLog[] selectJobLog(JobTraceQuery query) throws SQLException;

}
