package com.github.paganini2008.springdessert.webcrawler.es;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SearchResult
 *
 * @author Fred Feng
 * @since 1.0
 */
@Getter
@Setter
public class SearchResult implements Serializable {

	private static final long serialVersionUID = 5993548637885933491L;
	public static final String SEARCH_FIELD_TITLE = "title";
	public static final String SEARCH_FIELD_CONTENT = "content";
	public static final String SEARCH_FIELD_TYPE = "type";
	public static final String SEARCH_FIELD_SOURCE = "source";
	public static final String SEARCH_FIELD_VERSION = "version";

	private Long id;
	private String title;
	private String content;
	private String url;
	private String path;
	private String type;
	private String source;
	private Integer order;
	private Long createDate;
}