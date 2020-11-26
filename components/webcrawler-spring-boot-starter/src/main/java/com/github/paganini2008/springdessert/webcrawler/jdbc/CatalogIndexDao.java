package com.github.paganini2008.springdessert.webcrawler.jdbc;

import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INDEX_INSERT;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INDEX_MAX_VERSION;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INDEX_SELECT_ONE;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INDEX_UPDATE;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INDEX_VERSION_INCREMENT;

import com.github.paganini2008.springdessert.jdbc.annotations.Arg;
import com.github.paganini2008.springdessert.jdbc.annotations.Dao;
import com.github.paganini2008.springdessert.jdbc.annotations.Example;
import com.github.paganini2008.springdessert.jdbc.annotations.Get;
import com.github.paganini2008.springdessert.jdbc.annotations.Update;
import com.github.paganini2008.springdessert.webcrawler.model.CatalogIndex;

/**
 * 
 * CatalogIndexDao
 *
 * @author Fred Feng
 * @since 1.0
 */
@Dao
public interface CatalogIndexDao {

	@Update(SQL_CATALOG_INDEX_INSERT)
	int saveCatalogIndex(@Example CatalogIndex catalogIndex);

	@Update(SQL_CATALOG_INDEX_UPDATE)
	int updateCatalogIndex(@Example CatalogIndex catalogIndex);

	@Get(SQL_CATALOG_INDEX_SELECT_ONE)
	CatalogIndex getCatalogIndex(@Arg long catalogId);

	@Update(SQL_CATALOG_INDEX_VERSION_INCREMENT)
	int incrementCatalogIndexVersion(@Arg long catalogId);

	@Get(value = SQL_CATALOG_INDEX_MAX_VERSION, javaType = true)
	int maximunVersionOfCatalogIndex();

}
