package com.github.paganini2008.springdessert.webcrawler.jdbc;

import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_DELETE;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_INSERT;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_SELECT_ALL;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_SELECT_ONE;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_CATALOG_UPDATE;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.jdbc.annotations.Arg;
import com.github.paganini2008.springdessert.jdbc.annotations.Dao;
import com.github.paganini2008.springdessert.jdbc.annotations.Example;
import com.github.paganini2008.springdessert.jdbc.annotations.Get;
import com.github.paganini2008.springdessert.jdbc.annotations.Query;
import com.github.paganini2008.springdessert.jdbc.annotations.Update;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;

/**
 * 
 * CatalogDao
 *
 * @author Fred Feng
 * @since 1.0
 */
@Dao
public interface CatalogDao {

	@Update(SQL_CATALOG_INSERT)
	int saveCatalog(@Example Catalog catalog);

	@Update(SQL_CATALOG_UPDATE)
	int updateCatalog(@Example Catalog catalog);

	@Update(SQL_CATALOG_DELETE)
	int deleteCatalog(@Arg("id") long id);

	@Get(SQL_CATALOG_SELECT_ONE)
	Catalog getCatalog(@Arg("id") long id);

	@Query(SQL_CATALOG_SELECT_ALL)
	ResultSetSlice<Catalog> queryForCatalog();

}
