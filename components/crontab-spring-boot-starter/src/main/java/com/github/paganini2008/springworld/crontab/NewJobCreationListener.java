package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * NewJobCreationListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class NewJobCreationListener implements JobListener {

	@Qualifier("newJobObservable")
	@Autowired
	private Observable newJobObservable;

	@Override
	public void afterCreation(JobKey jobKey) {
		newJobObservable.notifyObservers(jobKey.getIndentifier(), jobKey);
	}

}
