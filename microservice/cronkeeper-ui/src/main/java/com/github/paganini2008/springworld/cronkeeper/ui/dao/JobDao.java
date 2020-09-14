package com.github.paganini2008.springworld.cronkeeper.ui.dao;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobInfo;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobTrace;
import com.github.paganini2008.springworld.fastjdbc.annotations.Arg;
import com.github.paganini2008.springworld.fastjdbc.annotations.Dao;
import com.github.paganini2008.springworld.fastjdbc.annotations.Get;
import com.github.paganini2008.springworld.fastjdbc.annotations.Slice;

/**
 * 
 * JobDao
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Dao
public interface JobDao {

	@Slice(value = "select a.*,b.trigger_type,c.job_state from ck_job_detail a join ck_job_trigger b on b.job_id=a.job_id join ck_job_runtime c on c.job_id=b.job_id", elementType = JobInfo.class)
	ResultSetSlice<JobInfo> selectJobInfo();

	@Get(value = "select a.cluster_name,a.group_name,a.job_name,a.job_class_name,b.*,c.* from ck_job_detail a join ck_job_trigger b on b.job_id=a.job_id join ck_job_runtime c on c.job_id=b.job_id where job_id=:jobId", javaType = false)
	JobDetail selectJobDetail(@Arg("jobId") int jobId);

	@Slice(value = "select * from ck_job_trace where job_id=:jobId", elementType = JobTrace.class)
	ResultSetSlice<JobTrace> selectJobTrace(@Arg("jobId") int jobId);

}
