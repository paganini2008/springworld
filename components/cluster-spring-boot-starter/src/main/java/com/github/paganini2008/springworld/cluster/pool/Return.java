package com.github.paganini2008.springworld.cluster.pool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Return
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class Return implements Signature {

	private Signature signature;
	private Object returnValue;

	public Return() {
	}

	public Return(Signature signature, Object returnValue) {
		this.signature = signature;
		this.returnValue = returnValue;
	}

	@JsonIgnore
	public String getId() {
		return signature.getId();
	}

	@JsonIgnore
	public String getBeanName() {
		return signature.getBeanName();
	}

	@JsonIgnore
	public String getBeanClassName() {
		return signature.getBeanClassName();
	}

	@JsonIgnore
	public Object[] getArguments() {
		return new Object[] { returnValue, signature };
	}

	@JsonIgnore
	public long getTimestamp() {
		return signature.getTimestamp();
	}

	public String getMethodName() {
		return null;
	}

}
