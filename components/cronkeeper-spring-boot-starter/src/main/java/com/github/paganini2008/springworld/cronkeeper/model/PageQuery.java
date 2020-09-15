package com.github.paganini2008.springworld.cronkeeper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * PageQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class PageQuery<T> extends Query {

	private int rows;
	private int page;
	private int size;
	private List<T> content = new ArrayList<T>();
	private boolean nextPage;

	public PageQuery() {
	}

	public PageQuery(int page, int size) {
		this.page = page;
		this.size = size;
	}

}
