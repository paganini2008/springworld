package com.github.paganini2008.springdessert.webcrawler.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * 
 * IndexedResourceRepository
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Component
public interface IndexedResourceRepository extends ElasticsearchRepository<IndexedResource, Long> {

}
