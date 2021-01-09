package com.github.paganini2008.springdessert.logtracker.es;

import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_MDC;
import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_MESSAGE;
import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_REASON;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * 
 * KeywordSearchResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class KeywordSearchResultSetSlice extends IndexSearchResultSetSlice {

	public KeywordSearchResultSetSlice(ElasticsearchTemplate elasticsearchTemplate, String keyword) {
		super(elasticsearchTemplate);
		this.keyword = keyword;
	}

	private final String keyword;

	@Override
	protected QueryBuilder buildQuery() {
		return QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(SEARCH_FIELD_MESSAGE, keyword))
				.should(QueryBuilders.matchQuery(SEARCH_FIELD_REASON, keyword)).should(QueryBuilders.matchQuery(SEARCH_FIELD_MDC, keyword));
	}

}
