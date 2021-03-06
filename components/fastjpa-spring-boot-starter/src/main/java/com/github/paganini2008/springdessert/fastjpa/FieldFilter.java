package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * 
 * FieldFilter
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public class FieldFilter<T> extends LogicalFilter {

	private final Field<T> field;
	private final PredicateBuilder<T> builder;

	FieldFilter(Field<T> field, PredicateBuilder<T> builder) {
		this.field = field;
		this.builder = builder;
	}

	public Predicate toPredicate(Model<?> model, CriteriaBuilder cb) {
		final Expression<T> expression = field.toExpression(model, cb);
		return builder.toPredicate(model, expression, cb);
	}

}
