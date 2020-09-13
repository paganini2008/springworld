package com.github.paganini2008.springworld.cronkeeper.ui.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cronkeeper.ui.dao.JobDao;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobInfo;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.ui.model.Page;

/**
 * 
 * JobQueryService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Component
public class JobQueryService {

	@Autowired
	private JobDao jobDao;

	public Page<JobInfo> selectJobInfo(int draw, int page, int size) {
		ResultSetSlice<JobInfo> resultSetSlice = jobDao.selectJobInfo();
		PageResponse<JobInfo> pageResponse = resultSetSlice.list(PageRequest.of(page, size));
		List<JobInfo> content = pageResponse.getContent();
		int rowCount = pageResponse.getTotalRecords();
		return new Page<JobInfo>(draw, rowCount, content);
	}

	public Page<JobTrace> selectJobTrace(int draw, int jobId, int page, int size) {
		ResultSetSlice<JobTrace> resultSetSlice = jobDao.selectJobTrace(jobId);
		PageResponse<JobTrace> pageResponse = resultSetSlice.list(PageRequest.of(page, size));
		List<JobTrace> content = pageResponse.getContent();
		int rowCount = pageResponse.getTotalRecords();
		return new Page<JobTrace>(draw, rowCount, content);
	}
	
	public JobDetail selectJobDetail(int jobId) {
		return jobDao.selectJobDetail(jobId);
	}

}
