package com.github.paganini2008.springdessert.jdbc;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.NotImplementedException;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.beans.PropertyUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.converter.ConvertUtils;
import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.PageableSql;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.jdbc.annotations.Arg;
import com.github.paganini2008.springdessert.jdbc.annotations.Example;
import com.github.paganini2008.springdessert.jdbc.annotations.Get;
import com.github.paganini2008.springdessert.jdbc.annotations.Insert;
import com.github.paganini2008.springdessert.jdbc.annotations.Query;
import com.github.paganini2008.springdessert.jdbc.annotations.Select;
import com.github.paganini2008.springdessert.jdbc.annotations.Update;

/**
 * 
 * DaoProxyBean
 *
 * @author Fred Feng
 * @since 1.0
 */
@SuppressWarnings("all")
public class DaoProxyBean<T> extends EnhancedJdbcDaoSupport implements InvocationHandler {

	private final Class<T> interfaceClass;
	protected final Logger log;

	public DaoProxyBean(DataSource dataSource, Class<T> interfaceClass) {
		Assert.isNull(dataSource, "DataSource must be required.");
		this.setDataSource(dataSource);
		this.interfaceClass = interfaceClass;
		this.log = LoggerFactory.getLogger(interfaceClass);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.isAnnotationPresent(Insert.class)) {
			return doInsert(method, args);
		} else if (method.isAnnotationPresent(Update.class)) {
			return doUpdate(method, args);
		} else if (method.isAnnotationPresent(Get.class)) {
			return doGet(method, args);
		} else if (method.isAnnotationPresent(Select.class)) {
			return doSelect(method, args);
		} else if (method.isAnnotationPresent(Query.class)) {
			return doQuery(method, args);
		}
		throw new NotImplementedException("Unknown target method: " + interfaceClass.getName() + "." + method.getName());
	}

	private Object doSelect(Method method, Object[] args) {
		Select select = method.getAnnotation(Select.class);
		String sql = select.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}
		if (!List.class.isAssignableFrom(method.getReturnType())) {
			throw new IllegalArgumentException("Return Type is only for List");
		}
		Type returnType = method.getGenericReturnType();
		ParameterizedType parameterizedType = (ParameterizedType) returnType;
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		Class<?> elementType = (Class<?>) actualTypeArguments[0];
		SqlParameterSource sqlParameterSource = getSqlParameterSource(method, args);
		if (select.singleColumn()) {
			return getNamedParameterJdbcTemplate().queryForList(sql, sqlParameterSource, elementType);
		} else {
			if (Map.class.isAssignableFrom(elementType)) {
				return getNamedParameterJdbcTemplate().queryForList(sql, sqlParameterSource);
			} else {
				return getNamedParameterJdbcTemplate().query(sql, sqlParameterSource, new BeanPropertyRowMapper<>(elementType));
			}
		}
	}

	private Object doQuery(Method method, Object[] args) {
		final Query query = method.getAnnotation(Query.class);
		PageableSql pageableSql = ApplicationContextUtils.getBean(query.pageableSql());
		if (pageableSql == null) {
			pageableSql = BeanUtils.instantiate(query.pageableSql(), query.value());
		}

		if (!ResultSetSlice.class.isAssignableFrom(method.getReturnType())) {
			throw new IllegalArgumentException("Return Type is only for ResultSetSlice");
		}
		Type returnType = method.getGenericReturnType();
		ParameterizedType parameterizedType = (ParameterizedType) returnType;
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		Class<?> elementType = (Class<?>) actualTypeArguments[0];
		SqlParameterSource sqlParameterSource = getSqlParameterSource(method, args);
		if (query.singleColumn()) {
			return getNamedParameterJdbcTemplate().slice(pageableSql, sqlParameterSource, elementType);
		} else {
			if (Map.class.isAssignableFrom(elementType)) {
				return getNamedParameterJdbcTemplate().slice(pageableSql, sqlParameterSource);
			} else {
				return getNamedParameterJdbcTemplate().slice(pageableSql, sqlParameterSource, new BeanPropertyRowMapper<>(elementType));
			}
		}
	}

	private Object doGet(Method method, Object[] args) {
		Get getter = method.getAnnotation(Get.class);
		String sql = getter.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}
		SqlParameterSource sqlParameterSource = getSqlParameterSource(method, args);
		if (getter.javaType()) {
			return getNamedParameterJdbcTemplate().queryForObject(sql, sqlParameterSource, returnType);
		} else {
			if (Map.class.isAssignableFrom(returnType)) {
				return getNamedParameterJdbcTemplate().queryForObject(sql, sqlParameterSource, new ColumnMapRowMapper());
			} else {
				return getNamedParameterJdbcTemplate().queryForObject(sql, sqlParameterSource, new BeanPropertyRowMapper<>(returnType));
			}
		}
	}

	private Object doInsert(Method method, Object[] args) {
		Insert insert = method.getAnnotation(Insert.class);
		String sql = insert.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}

		SqlParameterSource sqlParameterSource = getSqlParameterSource(method, args);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int effected = getNamedParameterJdbcTemplate().update(sql, sqlParameterSource, keyHolder);
		if (effected == 0) {
			throw new InvalidDataAccessResourceUsageException("Failed to insert a new record by sql: " + sql);
		}
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}
		Map<String, Object> keys = keyHolder.getKeys();
		Object value = CollectionUtils.getFirst(keys.values());
		try {
			return returnType.cast(value);
		} catch (RuntimeException e) {
			return ConvertUtils.convertValue(value, returnType);
		}
	}

	private Object doUpdate(Method method, Object[] args) {
		Update update = method.getAnnotation(Update.class);
		String sql = update.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}

		SqlParameterSource sqlParameterSource = getSqlParameterSource(method, args);
		int effectedRows = getNamedParameterJdbcTemplate().update(sql, sqlParameterSource);
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}
		try {
			return returnType.cast(effectedRows);
		} catch (RuntimeException e) {
			return ConvertUtils.convertValue(effectedRows, returnType);
		}
	}

	private SqlParameterSource getSqlParameterSource(Method method, Object[] args) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Parameter[] methodParameters = method.getParameters();
		Parameter methodParameter;
		Annotation[] annotations;
		Annotation annotation;
		for (int i = 0; i < methodParameters.length; i++) {
			methodParameter = methodParameters[i];
			annotations = methodParameter.getAnnotations();
			if (ArrayUtils.isEmpty(annotations)) {
				continue;
			}
			annotation = annotations[0];
			if (annotation instanceof Arg) {
				String key = ((Arg) annotation).value();
				if (StringUtils.isBlank(key)) {
					key = methodParameter.getName();
				}
				parameters.put(key, args[i]);
			} else if (annotation instanceof Example) {
				if (args[i] instanceof Map) {
					parameters.putAll((Map<String, Object>) args[i]);
				} else {
					parameters.putAll(PropertyUtils.convertToMap(args[i]));
				}
				String[] excludedProperties = ((Example) annotation).excludedProperties();
				if (ArrayUtils.isNotEmpty(excludedProperties)) {
					MapUtils.removeKeys(parameters, excludedProperties);
				}
			}
		}
		return new MapSqlParameterSource(parameters);
	}

	public Class<T> getInterfaceClass() {
		return interfaceClass;
	}

}
