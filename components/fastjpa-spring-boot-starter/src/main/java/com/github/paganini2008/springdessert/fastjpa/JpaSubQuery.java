package com.github.paganini2008.springdessert.fastjpa;

/**
 * 
 * JpaSubQuery
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public interface JpaSubQuery<E, T> extends JpaSubJoin<T>, SubQueryBuilder<T> {

	JpaSubQuery<E, T> filter(Filter filter);

	default JpaSubGroupBy<E, T> groupBy(String alias, String... attributeNames) {
		return groupBy(new FieldList().addFields(alias, attributeNames));
	}

	default JpaSubGroupBy<E, T> groupBy(Field<?>... fields) {
		return groupBy(new FieldList(fields));
	}

	JpaSubGroupBy<E, T> groupBy(FieldList fieldList);

	default JpaSubQuery<E, T> select(String attributeName) {
		return select(null, attributeName);
	}

	JpaSubQuery<E, T> select(String alias, String attributeName);

	JpaSubQuery<E, T> select(Field<T> field);

}
