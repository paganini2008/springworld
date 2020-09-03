package com.github.paganini2008.springworld.fastjpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Selection;

/**
 * 
 * JpaResultSetSlice
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JpaResultSetSlice<E, T> implements JpaPageQuery<T> {

	private final Model<E> model;
	private final CriteriaQuery<Tuple> query;
	private final JpaCustomQuery<?> customQuery;
	private final Transformer<E, T> transformer;

	JpaResultSetSlice(Model<E> model, CriteriaQuery<Tuple> query, JpaCustomQuery<?> customQuery, Transformer<E, T> transformer) {
		this.model = model;
		this.query = query;
		this.customQuery = customQuery;
		this.transformer = transformer;
	}

	private int totalRecords;

	public int rowCount() {
		return totalRecords;
	}

	public JpaPageQuery<T> setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
		return this;
	}

	public List<T> list(int maxResults) {
		return list(maxResults, 0);
	}

	public List<T> list(int maxResults, int firstResult) {
		List<Tuple> tuples = customQuery.getResultList(builder -> {
			return query;
		}, maxResults, firstResult);
		List<T> results = new ArrayList<T>();
		List<Selection<?>> selections = query.getSelection().getCompoundSelectionItems();
		for (Tuple tuple : tuples) {
			T entity = transformer.transfer(model, selections, tuple);
			results.add(entity);
		}
		return results;
	}
}
