package com.github.paganini2008.springdessert.jellyfish.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.jellyfish.ui.HistoryQuery;
import com.github.paganini2008.springdessert.jellyfish.ui.SearchQuery;
import com.github.paganini2008.springdessert.jellyfish.ui.SearchResult;

/**
 * 
 * LogEntrySearchService
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class LogEntrySearchService {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	public PageResponse<SearchResult> search(int page, int size) {
		return new MatchAllSearchResultSetSlice(elasticsearchTemplate).list(PageRequest.of(page, size));
	}

	public PageResponse<SearchResult> search(HistoryQuery searchQuery, int page, int size) {
		return new HistorySearchResultSetSlice(elasticsearchTemplate, searchQuery).list(PageRequest.of(page, size));
	}

	public PageResponse<SearchResult> search(SearchQuery searchQuery, int page, int size) {
		return new RealtimeSearchResultSetSlice(elasticsearchTemplate, searchQuery).list(PageRequest.of(page, size));
	}

}
