package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;

/**
 * 
 * JpaUpdateCallback
 * 
 * @author Fred Feng
 * 
 */
public interface JpaUpdateCallback<T> {

	CriteriaUpdate<T> doInJpa(CriteriaBuilder builder);

}
