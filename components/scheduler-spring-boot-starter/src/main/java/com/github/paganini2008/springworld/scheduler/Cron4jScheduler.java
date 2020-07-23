package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.cron4j.CRON;
import com.github.paganini2008.devtools.cron4j.Task;
import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.TaskExecutor.TaskFuture;

/**
 * 
 * Cron4jScheduler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class Cron4jScheduler implements Scheduler {

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private JobExecutor jobExecutor;

	@Value("${spring.application.cluster.scheduler.loadbalance.enabled:true}")
	private boolean loadbalanced;

	@Override
	public Future schedule(Job job, Object arg, String cron) {
		TaskFuture taskFuture = taskExecutor.schedule(wrapJob(job, arg), CRON.parse(cron));
		return new FutureImpl(taskFuture);
	}

	@Override
	public Future scheduleWithFixedDelay(Job job, Object arg, long delay, long period) {
		TaskFuture taskFuture = taskExecutor.scheduleWithFixedDelay(wrapJob(job, arg), delay, period);
		return new FutureImpl(taskFuture);
	}

	@Override
	public Future scheduleAtFixedRate(Job job, Object arg, long delay, long period) {
		TaskFuture taskFuture = taskExecutor.scheduleAtFixedRate(wrapJob(job, arg), delay, period);
		return new FutureImpl(taskFuture);
	}

	@Override
	public void runJob(Job job, Object arg) {
		jobExecutor.execute(job, arg);
	}

	protected Task wrapJob(final Job job, final Object arg) {
		return new Task() {

			@Override
			public boolean execute() {
				jobExecutor.execute(job, arg);
				return true;
			}
		};
	}

	private static class FutureImpl implements Future {

		private final TaskFuture taskFuture;

		FutureImpl(TaskFuture taskFuture) {
			this.taskFuture = taskFuture;
		}

		@Override
		public void cancel() {
			taskFuture.cancel();
		}

		@Override
		public boolean isDone() {
			return taskFuture.isDone();
		}

		@Override
		public long getNextExectionTime() {
			return taskFuture.getDetail().nextExectionTime();
		}

	}

}
