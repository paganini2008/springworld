package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * JobDependency
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobDependency extends Observable {

	public JobDependency() {
		super(true);
	}

	@Autowired
	private Scheduler scheduler;

	public void addDependency(Job job) {
		for (String signature : job.getDependencies()) {
			addObserver(signature, (ob, result) -> {
				scheduler.runJob(job, result);
			});
		}
	}

	public void notifyDependencies(Job job, Object result) {
		notifyObservers(job.getSignature(), result);
	}

}
