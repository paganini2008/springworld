package com.github.paganini2008.springdessert.fastjpa;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Selection;

/**
 * 
 * Transformer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Transformer<E, T> {

	T transfer(Model<E> model, List<Selection<?>> selections, Tuple tuple);

}
