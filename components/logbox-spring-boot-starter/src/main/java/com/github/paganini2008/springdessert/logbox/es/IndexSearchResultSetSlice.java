package com.github.paganini2008.springdessert.logbox.es;

import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_MDC;
import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_MESSAGE;
import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SEARCH_FIELD_REASON;
import static com.github.paganini2008.springdessert.logbox.ui.SearchResult.SORTED_FIELD_CREATE_TIME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.jdbc.PageableResultSetSlice;
import com.github.paganini2008.springdessert.logbox.ui.SearchResult;

/**
 * 
 * IndexSearchResultSetSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class IndexSearchResultSetSlice extends PageableResultSetSlice<SearchResult> {

	private final ElasticsearchTemplate elasticsearchTemplate;

	protected IndexSearchResultSetSlice(ElasticsearchTemplate elasticsearchTemplate) {
		this.elasticsearchTemplate = elasticsearchTemplate;
	}

	@Override
	public int rowCount() {
		QueryBuilder queryBuilder = buildQuery();
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
		return (int) elasticsearchTemplate.count(searchQuery, LogEntry.class);
	}

	@Override
	public List<SearchResult> list(int maxResults, int firstResult) {
		QueryBuilder filterBuilder = buildFilter();
		QueryBuilder queryBuilder = buildQuery();
		FieldSortBuilder sortBuilder = SortBuilders.fieldSort(SORTED_FIELD_CREATE_TIME).order(SortOrder.DESC);
		NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
		if (filterBuilder != null) {
			searchQueryBuilder.withFilter(filterBuilder);
		}
		searchQueryBuilder.withQuery(queryBuilder).withSort(sortBuilder)
				.withHighlightFields(new HighlightBuilder.Field(SEARCH_FIELD_MESSAGE), new HighlightBuilder.Field(SEARCH_FIELD_REASON),
						new HighlightBuilder.Field(SEARCH_FIELD_MDC))
				.withHighlightBuilder(new HighlightBuilder().preTags("<font class=\"searchKeyword\">").postTags("</font>").fragmentSize(20)
						.numOfFragments(3));

		if (maxResults > 0) {
			searchQueryBuilder.withPageable(PageRequest.of(getPageNumber() - 1, maxResults));
		}
		AggregatedPage<LogEntry> page = elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), LogEntry.class,
				new HighlightResultMapper(elasticsearchTemplate.getElasticsearchConverter().getMappingContext()));
		List<LogEntry> content = page.getContent();
		List<SearchResult> dataList = new ArrayList<SearchResult>();
		for (LogEntry logEntry : content) {
			dataList.add(BeanUtils.copy(logEntry, SearchResult.class, null));
		}
		if (tailLog()) {
			Collections.reverse(dataList);
		}
		return dataList;
	}

	protected QueryBuilder buildFilter() {
		return null;
	}

	protected abstract QueryBuilder buildQuery();

	protected boolean tailLog() {
		return true;
	}

}
