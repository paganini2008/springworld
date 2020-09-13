package com.github.paganini2008.springworld.cronfall;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cronfall.model.JobDetail;
import com.github.paganini2008.springworld.cronfall.model.JobInfo;
import com.github.paganini2008.springworld.cronfall.model.JobQuery;
import com.github.paganini2008.springworld.cronfall.model.JobRuntime;
import com.github.paganini2008.springworld.cronfall.model.JobStat;
import com.github.paganini2008.springworld.cronfall.model.JobTriggerDetail;

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

	JobDetail getJobDetail(JobKey jobKey) throws Exception;

	JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws Exception;

	boolean hasRelations(JobKey jobKey) throws Exception;

	JobKey[] getDependencies(JobKey jobKey) throws Exception;

	JobKey[] getJobKeys(JobQuery jobQuery) throws Exception;

	int getJobId(JobKey jobKey) throws Exception;

	JobRuntime getJobRuntime(JobKey jobKey) throws Exception;

	JobStat getJobStat(JobKey jobKey) throws Exception;

	ResultSetSlice<JobInfo> getJobInfo() throws Exception;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception;

}
