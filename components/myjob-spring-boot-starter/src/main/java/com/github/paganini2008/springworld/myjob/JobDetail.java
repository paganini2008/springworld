package com.github.paganini2008.springworld.myjob;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobDetail
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobDetail implements Serializable {

	private static final long serialVersionUID = 4349796691146506537L;
	private int jobId;
	private String jobName;
	private String jobClassName;
	private String groupName;
	private String description;
	private String attachment;
	private String email;
	private int retries;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;

}
