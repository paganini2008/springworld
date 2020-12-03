package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Subquery;

/**
 * 
 * SubQueryBuilder
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface SubQueryBuilder<T> {

	Subquery<T> toSubquery(CriteriaBuilder builder);

}
