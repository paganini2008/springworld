package com.github.paganini2008.springdessert.fastjpa;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Selection;

/**
 * 
 * AbstractTransformer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class AbstractTransformer<E, T> implements Transformer<E, T> {

	public T transfer(Model<E> model, List<Selection<?>> selections, Tuple tuple) {
		final T object = createObject(selections.size());
		for (Selection<?> selection : selections) {
			setAttributeValue(model, selection.getAlias(), selection.getJavaType(), tuple, object);
		}
		afterTransferring(model, tuple, object);
		return object;
	}

	protected abstract void setAttributeValue(Model<E> model, String attributeName, Class<?> javaType, Tuple tuple, T object);

	protected abstract T createObject(int columns);

	protected void afterTransferring(Model<E> model, Tuple tuple, T object) {
	}

}
