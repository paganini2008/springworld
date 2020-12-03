package com.github.paganini2008.springdessert.fastjpa;

/**
 * 
 * LogicalFilter
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public abstract class LogicalFilter implements Filter {

	public LogicalFilter and(Filter filter) {
		return new AndFilter(this, filter);
	}

	public LogicalFilter or(Filter filter) {
		return new OrFilter(this, filter);
	}

	public LogicalFilter not() {
		return new NotFilter(this);
	}

}
