package com.github.paganini2008.springdessert.webcrawler.jdbc;

import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_DELETE_ALL;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_INSERT;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_LATEST_PATH;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_SELECT_FOR_INDEX;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_SELECT_ONE;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_VERSION_UPDATE;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.jdbc.annotations.Arg;
import com.github.paganini2008.springdessert.jdbc.annotations.Dao;
import com.github.paganini2008.springdessert.jdbc.annotations.Example;
import com.github.paganini2008.springdessert.jdbc.annotations.Get;
import com.github.paganini2008.springdessert.jdbc.annotations.Query;
import com.github.paganini2008.springdessert.jdbc.annotations.Update;
import com.github.paganini2008.springdessert.webcrawler.model.Resource;

/**
 * 
 * ResourceDao
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Dao
public interface ResourceDao {

	@Update(SQL_RESOURCE_INSERT)
	int saveResource(@Example Resource resource);

	@Get(SQL_RESOURCE_SELECT_ONE)
	Resource getResource(@Arg("id") long id);

	@Query(SQL_RESOURCE_SELECT_FOR_INDEX)
	ResultSetSlice<Resource> queryForResourceForIndex(@Arg("catalogId") long catalogId);

	@Update(SQL_RESOURCE_VERSION_UPDATE)
	int updateResourceVersion(@Arg("catalogId") long catalogId, @Arg("version") int version);

	@Update(SQL_RESOURCE_DELETE_ALL)
	int deleteResourceByCatalogId(@Arg("catalogId") long catalogId);

	@Get(value = SQL_RESOURCE_LATEST_PATH, javaType = true)
	String getLatestPath(@Arg("catalogId") long catalogId);

}
