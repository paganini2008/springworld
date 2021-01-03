package com.github.paganini2008.springdessert.logtracker.es;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.logtracker.ui.SearchQuery;
import com.github.paganini2008.springdessert.logtracker.ui.SearchResult;

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

	public PageResponse<SearchResult> search(String keyword, SearchQuery searchQuery, int page, int size) {
		return search(keyword, searchQuery).list(PageRequest.of(page, size));
	}

	public ResultSetSlice<SearchResult> search(String keyword, SearchQuery searchQuery) {
		if (StringUtils.isNotBlank(keyword) || searchQuery != null) {
			return new KeywordSearchResultSetSlice(elasticsearchTemplate, keyword, searchQuery);
		}
		return new SearchAllResultSetSlice(elasticsearchTemplate);
	}

}
