package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * 
 * Filter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface Filter {

	public Predicate toPredicate(Model<?> model, CriteriaBuilder builder);

}
