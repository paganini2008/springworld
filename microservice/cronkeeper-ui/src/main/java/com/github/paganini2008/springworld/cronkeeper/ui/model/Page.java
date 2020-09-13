package com.github.paganini2008.springworld.cronkeeper.ui.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Page
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class Page<T> {

	private int draw;
	private int recordsTotal;
	private int recordsFiltered;
	private List<T> data = new ArrayList<T>();
	private String error;

	public Page() {
	}

	public Page(int draw, int recordsTotal, List<T> data) {
		this.draw = draw;
		this.recordsTotal = recordsTotal;
		this.recordsFiltered = recordsTotal;
		this.data = data;
	}

}
