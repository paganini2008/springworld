package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.springdessert.webcrawler.model.CatalogIndex;
import com.github.paganini2008.springdessert.webcrawler.model.Resource;

/**
 * 
 * ResourceManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface ResourceManager {

	long saveCatalog(Catalog catalog);

	int deleteCatalog(long id);

	Catalog getCatalog(long id);

	PageResponse<Catalog> queryForCatalog(int page, int size);

	int updateCatalogIndex(CatalogIndex catalogIndex);

	int maximumVersionOfCatalogIndex();

	CatalogIndex getCatalogIndex(long catalogId);

	int saveResource(Resource resource);

	Resource getResource(long id);

	PageResponse<Resource> queryForResourceForIndex(long catalogId, int page, int size);

	int updateResourceVersion(long catalogId, int version);

	int incrementCatalogIndexVersion(long catalogId);

}
