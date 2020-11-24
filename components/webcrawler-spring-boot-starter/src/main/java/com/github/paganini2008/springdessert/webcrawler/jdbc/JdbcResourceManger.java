package com.github.paganini2008.springdessert.webcrawler.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.webcrawler.ResourceManager;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.springdessert.webcrawler.model.CatalogIndex;
import com.github.paganini2008.springdessert.webcrawler.model.Resource;

/**
 * 
 * JdbcResourceManger
 *
 * @author Fred Feng
 * @since 1.0
 */
public class JdbcResourceManger implements ResourceManager {

	public static final String SQL_CATALOG_INSERT = "insert into crawler_catalog (id,name,url,path_pattern,excluded_path_pattern,type,last_modified) values (:id,:name,:url,:pathPattern,:excludedPathPattern,:type,:lastModified)";
	public static final String SQL_CATALOG_UPDATE = "update crawler_catalog set name=:name,url=:url,path_pattern=:pathPattern,excluded_path_pattern=:excludedPathPattern,type:type,last_modified=:lastModified where id=:id";
	public static final String SQL_CATALOG_INDEX_INSERT = "insert into crawler_catalog_index (id,catalog_id,version,last_indexed_date) values (:id,:catalogId,:version,:lastIndexedDate)";
	public static final String SQL_CATALOG_INDEX_UPDATE = "update crawler_catalog_index set version=:version,last_modified=:lastModified where id=:id";
	public static final String SQL_CATALOG_SELECT_ONE = "select * from crawler_catalog where id=:id limit 1";
	public static final String SQL_CATALOG_INDEX_SELECT_ONE = "select * from crawler_catalog_index where catalog_id=:catalogId";
	public static final String SQL_CATALOG_DELETE = "delete from crawler_catalog where id=:id";
	public static final String SQL_CATALOG_SELECT_ALL = "select * from crawler_catalog order by last_modified desc";
	public static final String SQL_RESOURCE_INSERT = "insert into crawler_resource (id,title,html,url,type,last_modified,version,catalog_id) values (:id,:title,:html,:url,:type,:lastModified,:version,:catalogId)";
	public static final String SQL_RESOURCE_SELECT_FOR_INDEX = "select * from crawler_resource where catalog_id=:catalogId and version<(select version from crawler_catalog_index where catalog_id=:catalogId)";
	public static final String SQL_RESOURCE_SELECT_ONE = "select * from crawler_resource where id=:id limit 1";
	public static final String SQL_RESOURCE_VERSION_UPDATE = "update crawler_resource set version=:version where catalog_id=:catalogId and version!=:version";

	@Autowired
	private CatalogDao catalogDao;

	@Autowired
	private CatalogIndexDao catalogIndexDao;

	@Autowired
	private ResourceDao resourceDao;

	@Override
	public int saveCatalog(Catalog catalog) {
		return catalogDao.saveCatalog(catalog);
	}

	@Override
	public int deleteCatalog(long id) {
		return catalogDao.deleteCatalog(id);
	}

	@Override
	public Catalog getCatalog(long id) {
		return catalogDao.getCatalog(id);
	}

	@Override
	public PageResponse<Catalog> queryForCatalog(int page, int size) {
		ResultSetSlice<Catalog> resultSetSlice = catalogDao.queryForCatalog();
		return resultSetSlice.list(PageRequest.of(page, size));
	}

	@Override
	public int updateCatalogIndex(CatalogIndex catalogIndex) {
		return catalogIndexDao.updateCatalogIndex(catalogIndex);
	}

	@Override
	public int saveCatalogIndex(CatalogIndex catalogIndex) {
		return catalogIndexDao.saveCatalogIndex(catalogIndex);
	}

	@Override
	public CatalogIndex getCatalogIndex(long catalogId) {
		return catalogIndexDao.getCatalogIndex(catalogId);
	}

	@Override
	public int saveResource(Resource resource) {
		return resourceDao.saveResource(resource);
	}

	@Override
	public Resource getResource(long id) {
		return resourceDao.getResource(id);
	}

	@Override
	public PageResponse<Resource> queryForResourceForIndex(long catalogId, int page, int size) {
		ResultSetSlice<Resource> resultSetSlice = resourceDao.queryForResourceForIndex(catalogId);
		return resultSetSlice.list(PageRequest.of(page, size));
	}

	@Override
	public int updateResourceVersion(long catalogId, int version) {
		// TODO Auto-generated method stub
		return 0;
	}

}
