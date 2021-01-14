package com.github.paganini2008.springdessert.logbox.es;

import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_MDC;
import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_MESSAGE;
import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_REASON;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.logbox.ui.SearchQuery;

/**
 * 
 * RealtimeSearchResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RealtimeSearchResultSetSlice extends IndexSearchResultSetSlice {

	private final SearchQuery searchQuery;

	public RealtimeSearchResultSetSlice(ElasticsearchTemplate elasticsearchTemplate, SearchQuery searchQuery) {
		super(elasticsearchTemplate);
		this.searchQuery = searchQuery;
	}

	@Override
	protected QueryBuilder buildFilter() {
		if (searchQuery != null) {
			BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
			if (StringUtils.isNotBlank(searchQuery.getClusterName())) {
				queryBuilder.filter(QueryBuilders.termQuery("clusterName", searchQuery.getClusterName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getApplicationName())) {
				queryBuilder.filter(QueryBuilders.termQuery("applicationName", searchQuery.getApplicationName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getHost())) {
				queryBuilder.filter(QueryBuilders.termQuery("host", searchQuery.getHost()));
			}
			if (StringUtils.isNotBlank(searchQuery.getLevel())) {
				queryBuilder.filter(QueryBuilders.termQuery("level", searchQuery.getLevel()));
			}
			if (StringUtils.isNotBlank(searchQuery.getLoggerName())) {
				queryBuilder.filter(QueryBuilders.termQuery("loggerName", searchQuery.getLoggerName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getMarker())) {
				queryBuilder.filter(QueryBuilders.termQuery("marker", searchQuery.getMarker()));
			}
			return queryBuilder;
		}
		return null;
	}

	@Override
	protected QueryBuilder buildQuery() {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		String keyword = searchQuery.getKeyword();
		if (StringUtils.isNotBlank(keyword)) {
			queryBuilder.should(QueryBuilders.matchQuery(SEARCH_FIELD_MESSAGE, keyword))
					.should(QueryBuilders.matchQuery(SEARCH_FIELD_REASON, keyword))
					.should(QueryBuilders.matchQuery(SEARCH_FIELD_MDC, keyword));
		}
		return queryBuilder;
	}

	@Override
	protected boolean tailLog() {
		return searchQuery.getAsc() != null ? searchQuery.getAsc().booleanValue() : false;
	}

}
