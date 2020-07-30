package com.github.paganini2008.springworld.scheduler;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobParameter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobParameter {

	private String signature;
	private Object argument;

	public JobParameter() {
	}

	public JobParameter(String signature, Object argument) {
		this.signature = signature;
		this.argument = argument;
	}

}
