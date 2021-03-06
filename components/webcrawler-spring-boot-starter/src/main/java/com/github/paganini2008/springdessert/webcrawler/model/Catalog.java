package com.github.paganini2008.springdessert.webcrawler.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Catalog
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Catalog implements Serializable {

	private static final long serialVersionUID = 1980884447290929341L;
	private Long id;
	private String name;
	private String cat;
	private String url;
	private String pageEncoding;
	private String pathPattern;
	private String excludedPathPattern;
	private Integer maxFetchSize;
	private Long duration;
	private Date lastModified;

}
