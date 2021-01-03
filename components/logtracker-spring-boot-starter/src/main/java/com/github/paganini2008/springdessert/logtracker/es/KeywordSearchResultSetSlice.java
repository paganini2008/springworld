package com.github.paganini2008.springdessert.logtracker.es;

import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_MDC;
import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_MESSAGE;
import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_REASON;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.logtracker.ui.SearchQuery;

/**
 * 
 * KeywordSearchResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class KeywordSearchResultSetSlice extends IndexSearchResultSetSlice {

	private final String keyword;
	private final SearchQuery searchQuery;

	public KeywordSearchResultSetSlice(ElasticsearchTemplate elasticsearchTemplate, String keyword, SearchQuery searchQuery) {
		super(elasticsearchTemplate);
		this.keyword = keyword;
		this.searchQuery = searchQuery;
	}

	@Override
	protected QueryBuilder buildQuery() {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (StringUtils.isNotBlank(keyword)) {
			queryBuilder = queryBuilder.should(QueryBuilders.matchQuery(SEARCH_FIELD_MESSAGE, keyword))
					.should(QueryBuilders.matchQuery(SEARCH_FIELD_REASON, keyword))
					.should(QueryBuilders.matchQuery(SEARCH_FIELD_MDC, keyword));
		}
		if (searchQuery != null) {
			if (StringUtils.isNotBlank(searchQuery.getClusterName())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("clusterName", searchQuery.getClusterName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getApplicationName())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("applicationName", searchQuery.getApplicationName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getHost())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("host", searchQuery.getHost()));
			}
			if (StringUtils.isNotBlank(searchQuery.getLevel())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("level", searchQuery.getLevel()));
			}
			if (StringUtils.isNotBlank(searchQuery.getLoggerName())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("loggerName", searchQuery.getLoggerName()));
			}
			if (StringUtils.isNotBlank(searchQuery.getMarker())) {
				queryBuilder = queryBuilder.filter(QueryBuilders.termQuery("marker", searchQuery.getMarker()));
			}
		}
		return queryBuilder;
	}

}
