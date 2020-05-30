package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * ConsistencyRequestConfirmationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestConfirmationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4041272418956233610L;

	public ConsistencyRequestConfirmationEvent(ConsistencyRequest request, ApplicationInfo applicationInfo, boolean ok) {
		super(request);
		this.applicationInfo = applicationInfo;
		this.ok = ok;
	}

	private final ApplicationInfo applicationInfo;
	private final boolean ok;

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public boolean isOk() {
		return ok;
	}

}
