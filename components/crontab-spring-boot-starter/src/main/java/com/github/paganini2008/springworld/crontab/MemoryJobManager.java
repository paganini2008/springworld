package com.github.paganini2008.springworld.crontab;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.devtools.scheduler.TaskExecutor;
import com.github.paganini2008.devtools.scheduler.TaskExecutor.TaskDetail;
import com.github.paganini2008.devtools.scheduler.TaskExecutor.TaskFuture;
import com.github.paganini2008.springworld.redisplus.BeanNames;
import com.github.paganini2008.springworld.redisplus.common.RedisHashSlice;
import com.github.paganini2008.devtools.scheduler.ThreadPoolTaskExecutor;

/**
 * 
 * MemoryJobManager
 *
 * @author Fred Feng
 * @version 1.0
 */
public class MemoryJobManager extends AbstractJobManager {

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.application.name}")
	private String applicationName;

	public MemoryJobManager() {
		this(new ThreadPoolTaskExecutor());
	}

	public MemoryJobManager(TaskExecutor taskExecutor) {
		super(taskExecutor);
	}

	@Override
	public void beforeJobExecution(TaskFuture future) {
		final Job job = (Job) future.getDetail().getTaskObject();
		final TaskDetail taskDetail = future.getDetail();

		JobInfo jobInfo = new JobInfo();
		jobInfo.setJobName(job.getName());
		jobInfo.setJobClass(job.getClass().getName());
		jobInfo.setDescription(job.getDescription());
		jobInfo.setRunning(taskDetail.isRunning());
		jobInfo.setPaused(future.isPaused());
		jobInfo.setCompletedCount(taskDetail.completedCount());
		jobInfo.setFailedCount(taskDetail.failedCount());
		jobInfo.setLastExecuted(new Date(taskDetail.lastExecuted()));
		jobInfo.setNextExecuted(new Date(taskDetail.nextExecuted()));
		jobInfo.setStartDate(startDate);

		String key = String.format("crontab:%s:", applicationName);
		redisTemplate.opsForHash().put(key, jobInfo.getJobName(), jobInfo);
	}

	@Override
	public void afterJobExecution(TaskFuture future, Throwable error) {
		beforeJobExecution(future);
	}

	@Override
	public ResultSetSlice<JobInfo> getJobInfos() {
		String key = String.format("crontab:%s:", applicationName);
		return new RedisHashSlice<JobInfo>(key, redisTemplate);
	}
}
