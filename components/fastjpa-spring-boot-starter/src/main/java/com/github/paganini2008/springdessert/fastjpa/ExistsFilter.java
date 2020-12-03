package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * 
 * ExistsFilter
 * 
 * @author Jimmy Hoff
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
