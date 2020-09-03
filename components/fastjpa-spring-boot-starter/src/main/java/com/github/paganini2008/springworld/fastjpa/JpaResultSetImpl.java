package com.github.paganini2008.springworld.fastjpa;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;

import com.github.paganini2008.devtools.converter.ConvertUtils;

/**
 * 
 * JpaResultSetImpl
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JpaResultSetImpl<E> implements JpaResultSet<E> {

	private final Model<E> model;
	private final CriteriaQuery<Tuple> query;
	private final JpaCustomQuery<?> customQuery;

	JpaResultSetImpl(Model<E> model, CriteriaQuery<Tuple> query, JpaCustomQuery<?> customQuery) {
		this.model = model;
		this.query = query;
		this.customQuery = customQuery;
	}

	private int totalRecords;

	public JpaPageQuery<E> setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
		return this;
	}

	public <T> T getResult(Class<T> requiredType) {
		Tuple tuple = customQuery.getSingleResult(builder -> {
			return query;
		});
		Object result = tuple.get(0);
		if (result != null) {
			try {
				return requiredType.cast(result);
			} catch (RuntimeException e) {
				result = ConvertUtils.convertValue(result, requiredType);
				return requiredType.cast(result);
			}
		}
		return null;
	}

	public int rowCount() {
		return totalRecords;
	}

	public List<E> list(int maxResults, int firstResult) {
		return setTransformer(Transformers.asBean(model.getType())).list(maxResults, firstResult);
	}

	public <T> JpaPageQuery<T> setTransformer(Transformer<E, T> transformer) {
		return new JpaResultSetSlice<E, T>(model, query, customQuery, transformer);
	}
}
