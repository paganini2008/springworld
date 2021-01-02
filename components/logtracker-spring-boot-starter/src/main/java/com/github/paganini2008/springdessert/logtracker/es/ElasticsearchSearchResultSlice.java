package com.github.paganini2008.springdessert.logtracker.es;

import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_MESSAGE;
import static com.github.paganini2008.springdessert.logtracker.ui.SearchResult.SEARCH_FIELD_REASON;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.jdbc.PageableSlice;
import com.github.paganini2008.springdessert.logtracker.ui.SearchResult;

/**
 * 
 * ElasticsearchSearchResultSlice
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ElasticsearchSearchResultSlice extends PageableSlice<SearchResult> {

	private final String keyword;
	private final ElasticsearchTemplate elasticsearchTemplate;

	public ElasticsearchSearchResultSlice(String keyword, ElasticsearchTemplate elasticsearchTemplate) {
		this.keyword = keyword;
		this.elasticsearchTemplate = elasticsearchTemplate;
	}

	@Override
	public int rowCount() {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(SEARCH_FIELD_MESSAGE, keyword))
				.must(QueryBuilders.matchQuery(SEARCH_FIELD_REASON, keyword));
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).build();
		return (int) elasticsearchTemplate.count(searchQuery, LogEntry.class);
	}

	@Override
	public List<SearchResult> list(int maxResults, int firstResult) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(SEARCH_FIELD_MESSAGE, keyword))
				.must(QueryBuilders.matchQuery(SEARCH_FIELD_REASON, keyword));
		NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
				.withHighlightFields(new HighlightBuilder.Field(SEARCH_FIELD_MESSAGE), new HighlightBuilder.Field(SEARCH_FIELD_REASON))
				.withHighlightBuilder(new HighlightBuilder().preTags("<font class=\"search-keyword\" color=\"#FF0000\">")
						.postTags("</font>").fragmentSize(10).numOfFragments(3).noMatchSize(150));
		if (maxResults > 0) {
			searchQueryBuilder = searchQueryBuilder.withPageable(PageRequest.of(getPageNumber(), maxResults));
		}
		AggregatedPage<LogEntry> page = elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), LogEntry.class,
				new HighlightResultMapper(elasticsearchTemplate.getElasticsearchConverter().getMappingContext()));
		List<LogEntry> content = page.getContent();
		List<SearchResult> dataList = new ArrayList<SearchResult>();
		for (LogEntry resource : content) {
			dataList.add(BeanUtils.copy(resource, SearchResult.class, null));
		}
		return dataList;
	}

}
