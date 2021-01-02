package com.github.paganini2008.springdessert.logtracker.ui;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SearchResult
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
public class SearchResult implements Serializable{

	private static final long serialVersionUID = 8569951355416294604L;
	
	public static final String SEARCH_FIELD_MESSAGE = "message";
	public static final String SEARCH_FIELD_REASON = "reason";
	
	private Long id;
	private String loggerName;
	private String message;
	private String level;
	private String reason;
	private String mdc;
	private String marker;
	private long createTime;
}
