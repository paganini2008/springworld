package com.github.paganini2008.springdessert.cluster;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Contact
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class Contact implements Serializable{

	private static final long serialVersionUID = 4110243793757357219L;
	private String name;
	private String campany;
	private String department;
	private String position;
	private String url;
	private String email;
	private String phone;
	private String description;

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
