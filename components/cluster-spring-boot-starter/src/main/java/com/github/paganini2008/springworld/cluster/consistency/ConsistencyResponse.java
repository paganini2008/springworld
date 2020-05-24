package com.github.paganini2008.springworld.cluster.consistency;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * ConsistencyResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@ToString
@Setter
@Getter
public class ConsistencyResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	public ConsistencyResponse() {
	}

	public ConsistencyResponse(ConsistencyRequest request, String instanceId, boolean acceptable) {
		this.request = request;
		this.instanceId = instanceId;
		this.acceptable = acceptable;
	}

	private ConsistencyRequest request;
	private String instanceId;
	private boolean acceptable;

}
