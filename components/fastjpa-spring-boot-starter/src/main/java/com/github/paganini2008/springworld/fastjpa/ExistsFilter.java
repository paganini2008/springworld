package com.github.paganini2008.springworld.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * 
 * ExistsFilter
 * 
 * @author Fred Feng
 * 
 * 
 */
public class ExistsFilter extends LogicalFilter {

	private final SubQueryBuilder<?> queryBuiler;

	ExistsFilter(SubQueryBuilder<?> queryBuiler) {
		this.queryBuiler = queryBuiler;
	}

	public Predicate toPredicate(Model<?> selector, CriteriaBuilder builder) {
		return builder.exists(queryBuiler.toSubquery(builder));
	}

}
