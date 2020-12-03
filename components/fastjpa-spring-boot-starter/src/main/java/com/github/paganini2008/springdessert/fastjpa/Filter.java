package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * 
 * Filter
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public interface Filter {

	public Predicate toPredicate(Model<?> model, CriteriaBuilder builder);

}
