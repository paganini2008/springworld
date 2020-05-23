package com.github.paganini2008.springworld.cluster.consistency;

import java.io.Serializable;

import lombok.Getter;

/**
 * 
 * ConsistencyResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@Getter
public class ConsistencyResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	public ConsistencyResponse(ConsistencyRequest request, String instanceId, boolean acceptable) {
		this.request = request;
		this.instanceId = instanceId;
		this.acceptable = acceptable;
	}

	private final ConsistencyRequest request;
	private final String instanceId;
	private final boolean acceptable;

}
