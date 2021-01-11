package com.github.paganini2008.springdessert.logbox.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.springdessert.logbox.ui.HistoryQuery;

/**
 * 
 * HistorySearchResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class HistorySearchResultSetSlice extends RealtimeSearchResultSetSlice {

	public HistorySearchResultSetSlice(ElasticsearchTemplate elasticsearchTemplate, HistoryQuery searchQuery) {
		super(elasticsearchTemplate, searchQuery);
		this.searchQuery = searchQuery;
	}

	private final HistoryQuery searchQuery;

	@Override
	protected QueryBuilder buildFilter() {
		BoolQueryBuilder queryBuilder = (BoolQueryBuilder) super.buildFilter();
		RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("createTime");
		long startTime = searchQuery.getStartDate() != null ? searchQuery.getStartDate().getTime() : 0;
		long endTime = searchQuery.getEndDate() != null ? searchQuery.getEndDate().getTime() : 0;
		if (startTime == 0 && endTime == 0) {
			endTime = System.currentTimeMillis();
		}
		if (startTime > 0) {
			rangeQuery.gte(startTime);
		}
		if (endTime > 0) {
			rangeQuery.lte(endTime);
		}
		queryBuilder.filter(rangeQuery);
		return queryBuilder;
	}

}
