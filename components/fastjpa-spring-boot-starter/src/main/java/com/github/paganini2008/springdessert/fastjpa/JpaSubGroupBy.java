package com.github.paganini2008.springdessert.fastjpa;

/**
 * 
 * JpaSubGroupBy
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public interface JpaSubGroupBy<E, T> {

	JpaSubGroupBy<E, T> having(Filter filter);

	default JpaSubGroupBy<E, T> select(String attributeName){
		return select(null, attributeName);
	}

	JpaSubGroupBy<E, T> select(String alias, String attributeName);

	JpaSubGroupBy<E, T> select(Field<T> field);

}
