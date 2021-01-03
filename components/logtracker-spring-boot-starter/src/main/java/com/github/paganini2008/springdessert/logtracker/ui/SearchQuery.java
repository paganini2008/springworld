package com.github.paganini2008.springdessert.logtracker.ui;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SearchQuery
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
public class SearchQuery {

	private String clusterName;
	private String applicationName;
	private String host;
	private String identifier;
	private String loggerName;
	private String level;
	private String marker;
}
