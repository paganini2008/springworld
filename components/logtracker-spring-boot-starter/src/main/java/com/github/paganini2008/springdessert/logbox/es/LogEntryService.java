package com.github.paganini2008.springdessert.logbox.es;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.logbox.ui.SearchQuery;
import com.github.paganini2008.springdessert.logbox.ui.SearchResult;

/**
 * 
 * LogEntryService
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class LogEntryService {

	@Autowired
	private LogEntryRepository logEntryRepository;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	public void saveLogEntry(LogEntry logEntry) {
		logEntryRepository.save(logEntry);
	}

	public PageResponse<SearchResult> search(int page, int size) {
		return new MatchAllSearchResultSetSlice(elasticsearchTemplate).list(PageRequest.of(page, size));
	}

	public PageResponse<SearchResult> search(String keyword, int page, int size) {
		return new KeywordSearchResultSetSlice(elasticsearchTemplate, keyword).list(PageRequest.of(page, size));
	}

	public PageResponse<SearchResult> search(SearchQuery searchQuery, int page, int size) {
		return new ConditionalSearchResultSetSlice(elasticsearchTemplate, searchQuery).list(PageRequest.of(page, size));
	}

}
