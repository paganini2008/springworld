package com.github.paganini2008.springdessert.logtracker.es;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * 
 * SearchAllResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class SearchAllResultSetSlice extends IndexSearchResultSetSlice {

	public SearchAllResultSetSlice(ElasticsearchTemplate elasticsearchTemplate) {
		super(elasticsearchTemplate);
	}

	@Override
	protected QueryBuilder buildQuery() {
		return QueryBuilders.matchAllQuery();
	}

}
