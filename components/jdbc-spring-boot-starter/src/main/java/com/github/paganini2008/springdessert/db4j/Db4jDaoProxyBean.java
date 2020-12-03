package com.github.paganini2008.springdessert.db4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.NotImplementedException;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.beans.PropertyUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.converter.ConvertUtils;
import com.github.paganini2008.devtools.db4j.GeneratedKey;
import com.github.paganini2008.devtools.db4j.MapSqlParameter;
import com.github.paganini2008.devtools.db4j.SqlParameter;
import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.devtools.db4j.mapper.BeanPropertyRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.ColumnIndexRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.MapRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.TupleRowMapper;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.PageableSql;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.jdbc.ApplicationContextUtils;
import com.github.paganini2008.springdessert.jdbc.annotations.Arg;
import com.github.paganini2008.springdessert.jdbc.annotations.Example;
import com.github.paganini2008.springdessert.jdbc.annotations.Get;
import com.github.paganini2008.springdessert.jdbc.annotations.Insert;
import com.github.paganini2008.springdessert.jdbc.annotations.Query;
import com.github.paganini2008.springdessert.jdbc.annotations.Select;
import com.github.paganini2008.springdessert.jdbc.annotations.Update;

/**
 * 
 * Db4jDaoProxyBean
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@SuppressWarnings("all")
public class Db4jDaoProxyBean<T> extends SqlPlus implements InvocationHandler {

	private final Class<T> interfaceClass;
	protected final Logger log;

	public Db4jDaoProxyBean(ConnectionFactory connectionFactory, Class<T> interfaceClass) {
		super(connectionFactory);
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

	private Object doSelect(Method method, Object[] args) throws SQLException {
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
		SqlParameter sqlParameter = getSqlParameter(method, args);
		if (select.singleColumn()) {
			return queryForList(sql, sqlParameter, new ColumnIndexRowMapper<>(elementType));
		} else {
			if (Tuple.class.isAssignableFrom(elementType)) {
				return queryForList(sql, sqlParameter);
			} else if (Map.class.isAssignableFrom(elementType)) {
				return queryForList(sql, sqlParameter, new MapRowMapper());
			} else {
				return queryForList(sql, sqlParameter, new BeanPropertyRowMapper<>(elementType));
			}
		}
	}

	private Object doQuery(Method method, Object[] args) throws SQLException {
		final Query query = method.getAnnotation(Query.class);
		PageableSql pageableSql = ApplicationContextUtils.getBean(query.pageableSql());
		if (pageableSql == null) {
			pageableSql = new DefaultPageableSql(query.value());
		}
		if (!ResultSetSlice.class.isAssignableFrom(method.getReturnType())) {
			throw new IllegalArgumentException("Return Type is only for ResultSetSlice");
		}
		Type returnType = method.getGenericReturnType();
		ParameterizedType parameterizedType = (ParameterizedType) returnType;
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		Class<?> elementType = (Class<?>) actualTypeArguments[0];
		SqlParameter sqlParameter = getSqlParameter(method, args);
		if (query.singleColumn()) {
			return queryForPage(pageableSql, sqlParameter, new ColumnIndexRowMapper<>(elementType));
		} else {
			if (Tuple.class.isAssignableFrom(elementType)) {
				return queryForPage(pageableSql, sqlParameter);
			} else if (Map.class.isAssignableFrom(elementType)) {
				return queryForPage(pageableSql, sqlParameter, new MapRowMapper());
			} else {
				return queryForPage(pageableSql, sqlParameter, new BeanPropertyRowMapper<>(elementType));
			}
		}
	}

	private Object doGet(Method method, Object[] args) throws SQLException {
		Get getter = method.getAnnotation(Get.class);
		String sql = getter.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}
		SqlParameter sqlParameter = getSqlParameter(method, args);
		if (getter.javaType()) {
			return queryForObject(sql, sqlParameter, new ColumnIndexRowMapper<>(returnType));
		} else {
			if (Tuple.class.isAssignableFrom(returnType)) {
				return queryForObject(sql, sqlParameter, new TupleRowMapper());
			} else if (Map.class.isAssignableFrom(returnType)) {
				return queryForObject(sql, sqlParameter, new MapRowMapper());
			} else {
				return queryForObject(sql, sqlParameter, new BeanPropertyRowMapper<>(returnType));
			}
		}
	}

	private Object doInsert(Method method, Object[] args) throws SQLException {
		Insert insert = method.getAnnotation(Insert.class);
		String sql = insert.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}

		SqlParameter sqlParameter = getSqlParameter(method, args);
		GeneratedKey generatedKey = GeneratedKey.autoGenerated();
		int effected = update(sql, sqlParameter, generatedKey);
		if (effected == 0) {
			throw new IllegalStateException("Failed to insert a new record by sql: " + sql);
		}
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}
		Map<String, Object> keys = generatedKey.getKeys();
		if (keys.isEmpty()) {
			throw new NoGeneratedKeyException();
		}
		Object value = CollectionUtils.getFirst(keys.values());
		try {
			return returnType.cast(value);
		} catch (RuntimeException e) {
			return ConvertUtils.convertValue(value, returnType);
		}
	}

	private Object doUpdate(Method method, Object[] args) throws SQLException {
		Update update = method.getAnnotation(Update.class);
		String sql = update.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}

		SqlParameter sqlParameter = getSqlParameter(method, args);
		int effectedRows = update(sql, sqlParameter);
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

	private SqlParameter getSqlParameter(Method method, Object[] args) {
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
		return new MapSqlParameter(parameters);
	}

	public Class<T> getInterfaceClass() {
		return interfaceClass;
	}

	public String toString() {
		return interfaceClass.getName() + "$ProxyByJDK";
	}

}
