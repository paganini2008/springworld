package com.github.paganini2008.springdessert.jobsoup.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springdessert.jobsoup.JobException;
import com.github.paganini2008.springdessert.jobsoup.SqlScripts;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.Contact;
import com.github.paganini2008.springworld.cluster.utils.BeanLifeCycle;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ClusterRegistry
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ClusterRegistry implements BeanLifeCycle {

	@Autowired
	private ConnectionFactory connectionFactory;

	@Override
	public void configure() throws Exception {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_CLEAN_CLUSTER_DETAIL);
			log.info("Clean up cluster registry ok.");
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	public void registerCluster(ApplicationInfo applicationInfo) {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_CLUSTER_DETAIL, ps -> {
				ps.setString(1, applicationInfo.getClusterName());
				ps.setString(2, applicationInfo.getApplicationName());
				ps.setString(3, applicationInfo.getId());
				ps.setString(4, applicationInfo.getApplicationContextPath());
				ps.setTimestamp(5, new Timestamp(applicationInfo.getStartTime()));
				Contact contact = applicationInfo.getContact();
				if (contact != null) {
					ps.setString(6, contact.getName());
					ps.setString(7, contact.getEmail());
				} else {
					ps.setString(6, null);
					ps.setString(7, null);
				}
			});
			log.info("Registered cluster: " + applicationInfo.getClusterName());
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	public void unregisterCluster(String clusterName) {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_DELETE_CLUSTER_DETAIL, new Object[] { clusterName });
			log.info("Unregistered cluster: " + clusterName);
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	public String[] getClusterContextPaths(String clusterName) {
		List<String> results = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_CLUSTER_CONTEXT_PATH, new Object[] { clusterName });
			for (Tuple tuple : dataList) {
				results.add(tuple.getProperty("contextPath"));
			}
			return results.toArray(new String[0]);
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

}
