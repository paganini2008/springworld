package com.github.paganini2008.springdessert.webcrawler.jdbc;

import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.SQL_RESOURCE_INSERT;
import static com.github.paganini2008.springdessert.webcrawler.jdbc.JdbcResourceManger.*;

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
 * @author Fred Feng
 * @since 1.0
 */
@Dao
public interface ResourceDao {

	@Update(SQL_RESOURCE_INSERT)
	int saveResource(@Example Resource resource);

	@Get(SQL_RESOURCE_SELECT_ONE)
	Resource getResource(@Arg long id);

	@Query(SQL_RESOURCE_SELECT_FOR_INDEX)
	ResultSetSlice<Resource> queryForResourceForIndex(@Arg long catalogId);
	
	

}
