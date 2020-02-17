package com.github.paganini2008.springworld.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * 
 * JpaQueryCallback
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface JpaQueryCallback<T> {

	CriteriaQuery<T> doInJpa(CriteriaBuilder builder);

}
