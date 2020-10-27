package com.github.paganini2008.springworld.jobstorm;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.PooledConnectionFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobManagerConnectionFactory
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobManagerConnectionFactory extends PooledConnectionFactory implements LifeCycle {

	private static final Map<String, String> ddls = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("js_server_detail", SqlScripts.DEF_DDL_CLUSTER_DETAIL);
			put("js_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("js_job_trigger", SqlScripts.DEF_DDL_JOB_TRIGGER);
			put("js_job_runtime", SqlScripts.DEF_DDL_JOB_RUNTIME);
			put("js_job_trace", SqlScripts.DEF_DDL_JOB_TRACE);
			put("js_job_exception", SqlScripts.DEF_DDL_JOB_EXCEPTION);
			put("js_job_log", SqlScripts.DEF_DDL_JOB_LOG);
			put("js_job_dependency", SqlScripts.DEF_DDL_JOB_DEPENDENCY);
		}
	};

	public JobManagerConnectionFactory(DataSource dataSource) {
		super(dataSource);
	}

	@Value("${jobstorm.jdbc.createTable:true}")
	private boolean createTable;

	@PostConstruct
	@Override
	public void configure() throws Exception {
		if (createTable) {
			Connection connection = null;
			try {
				connection = getConnection();
				for (Map.Entry<String, String> entry : new HashMap<String, String>(ddls).entrySet()) {
					if (!JdbcUtils.existsTable(connection, null, entry.getKey())) {
						JdbcUtils.update(connection, entry.getValue());
						log.info("Create table: " + entry.getKey());
					}
				}
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
	}

	@PreDestroy
	@Override
	public void close() {
		super.close();
	}

}
