package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

/**
 * 
 * Column
 * 
 * @author Jimmy Hoff
 * 
 * 
 */
public interface Column {

	Selection<?> toSelection(Model<?> model, CriteriaBuilder builder);

	static Column forName(String attributeName) {
		return forName(null, attributeName);
	}

	static Column forName(String alias, String attributeName) {
		return forName(alias, attributeName, null);
	}

	static Column forName(String attributeName, Class<?> requiredType) {
		return forName(null, attributeName, requiredType);
	}

	static Column forName(String alias, String attributeName, Class<?> requiredType) {
		return Property.forName(alias, attributeName, requiredType).as(attributeName);
	}

}
