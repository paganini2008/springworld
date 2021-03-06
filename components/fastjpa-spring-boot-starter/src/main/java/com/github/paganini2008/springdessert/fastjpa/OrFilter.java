package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * 
 * OrFilter
 * 
 * @author Jimmy Hoff
 * 
 */
public class OrFilter extends LogicalFilter {

	private final Filter filter;

	private final Filter otherFilter;

	public OrFilter(Filter filter, Filter otherFilter) {
		this.filter = filter;
		this.otherFilter = otherFilter;
	}

	public Predicate toPredicate(Model<?> selector, CriteriaBuilder builder) {
		Predicate left = filter.toPredicate(selector, builder);
		Predicate right = otherFilter.toPredicate(selector, builder);
		return builder.or(new Predicate[] { left, right });
	}

}
