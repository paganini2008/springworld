package com.github.paganini2008.springdessert.logbox.ui;

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
public class SearchResult implements Serializable {

	private static final long serialVersionUID = 8569951355416294604L;

	public static final String SEARCH_FIELD_MESSAGE = "message";
	public static final String SEARCH_FIELD_REASON = "reason";
	public static final String SEARCH_FIELD_MDC = "mdc";
	public static final String SORTED_FIELD_CREATE_TIME = "createTime";

	private Long id;
	private String clusterName;
	private String applicationName;
	private String host;
	private String identifier;
	private String loggerName;
	private String message;
	private String level;
	private String[] stackTraces;
	private String mdc;
	private String marker;
	private String datetime;
}
