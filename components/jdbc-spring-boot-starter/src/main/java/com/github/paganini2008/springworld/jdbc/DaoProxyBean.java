package com.github.paganini2008.springworld.jdbc;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.NotImplementedException;
import com.github.paganini2008.devtools.beans.PropertyUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.converter.ConvertUtils;
import com.github.paganini2008.devtools.db4j.GeneratedKey;
import com.github.paganini2008.devtools.db4j.JdbcOperations;
import com.github.paganini2008.devtools.db4j.MapSqlParameter;
import com.github.paganini2008.devtools.db4j.SqlParameter;
import com.github.paganini2008.devtools.db4j.mapper.BeanPropertyRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.ColumnIndexRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.MapRowMapper;
import com.github.paganini2008.devtools.db4j.mapper.TupleRowMapper;
import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.PageableSql;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.tx.Session;
import com.github.paganini2008.springworld.tx.SessionManager;

/**
 * 
 * DaoProxyBean
 *
 * @author Fred Feng
 * @version 1.0
 */
@SuppressWarnings("all")
public class DaoProxyBean<T> implements InvocationHandler {

	private final Class<T> interfaceClass;
	protected final Logger log;

	public DaoProxyBean(Class<T> interfaceClass, SessionManager sessionManager) {
		Assert.isNull(sessionManager, "SessionManager must be required.");
		this.interfaceClass = interfaceClass;
		this.log = LoggerFactory.getLogger(interfaceClass);
		this.sessionManager = sessionManager;
	}

	private final SessionManager sessionManager;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!acceptedMethod(method)) {
			throw new IllegalArgumentException("All calling is only based in Dao operations.");
		}
		if (method.isAnnotationPresent(Insert.class)) {
			return doInsert(method, args);
		} else if (method.isAnnotationPresent(Update.class)) {
			return doUpdate(method, args);
		} else if (method.isAnnotationPresent(Get.class)) {
			return doGet(method, args);
		} else if (method.isAnnotationPresent(Select.class)) {
			return doSelect(method, args);
		} else if (method.isAnnotationPresent(Slice.class)) {
			return doSlice(method, args);
		}
		throw new NotImplementedException("Unkown method: " + interfaceClass.getName() + "." + method.getName());
	}

	private Object doSelect(Method method, Object[] args) throws SQLException {
		Select select = method.getAnnotation(Select.class);
		String sql = select.value();
		if (log.isTraceEnabled()) {
			log.trace("Execute sql: " + sql);
		}

		Class<?> returnType = method.getReturnType();
		if (!List.class.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException("Only for List Type");
		}
		SqlParameter sqlParameter = getSqlParameter(method, args);
		Class<?> elementType = select.elementType();
		JdbcOperations jdbcOperations = getJdbcOperations();
		if (select.javaType()) {
			return jdbcOperations.queryForList(sql, sqlParameter, new ColumnIndexRowMapper<>(elementType));
		} else {
			if (Tuple.class.isAssignableFrom(elementType)) {
				return jdbcOperations.queryForList(sql, sqlParameter);
			} else if (Map.class.isAssignableFrom(elementType)) {
				return jdbcOperations.queryForList(sql, sqlParameter, new MapRowMapper());
			} else {
				return jdbcOperations.queryForList(sql, sqlParameter, new BeanPropertyRowMapper<>(elementType));
			}
		}
	}

	private Object doSlice(Method method, Object[] args) throws SQLException {
		final Slice slice = method.getAnnotation(Slice.class);
		PageableSql pageableSql;
		if (slice.pageableSql() != null && slice.pageableSql() != Void.class) {
			try {
				Object bean = ApplicationContextUtils.getBean(slice.pageableSql());
				pageableSql = PageableSql.class.cast(bean);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		} else {
			pageableSql = new DefaultPageableSql(slice.value());
		}

		Class<?> resultType = method.getReturnType();
		if (!ResultSetSlice.class.isAssignableFrom(resultType)) {
			throw new IllegalArgumentException("Only for ResultSetSlice Type");
		}
		SqlParameter sqlParameter = getSqlParameter(method, args);
		Class<?> elementType = slice.elementType();
		JdbcOperations jdbcOperations = getJdbcOperations();
		if (slice.javaType()) {
			return jdbcOperations.queryForPage(pageableSql, sqlParameter, new ColumnIndexRowMapper<>(elementType));
		} else {
			if (Tuple.class.isAssignableFrom(elementType)) {
				return jdbcOperations.queryForPage(pageableSql, sqlParameter);
			} else if (Map.class.isAssignableFrom(elementType)) {
				return jdbcOperations.queryForPage(pageableSql, sqlParameter, new MapRowMapper());
			} else {
				return jdbcOperations.queryForPage(pageableSql, sqlParameter, new BeanPropertyRowMapper<>(elementType));
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
		JdbcOperations jdbcOperations = getJdbcOperations();
		if (getter.javaType()) {
			return jdbcOperations.queryForObject(sql, sqlParameter, new ColumnIndexRowMapper<>(returnType));
		} else {
			if (Tuple.class.isAssignableFrom(returnType)) {
				return jdbcOperations.queryForObject(sql, sqlParameter, new TupleRowMapper());
			} else if (Map.class.isAssignableFrom(returnType)) {
				return jdbcOperations.queryForObject(sql, sqlParameter, new MapRowMapper());
			} else {
				return jdbcOperations.queryForObject(sql, sqlParameter, new BeanPropertyRowMapper<>(returnType));
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
		JdbcOperations jdbcOperations = getJdbcOperations();
		GeneratedKey generatedKey = GeneratedKey.auto();
		int effected = jdbcOperations.update(sql, sqlParameter, generatedKey);
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
		JdbcOperations jdbcOperations = getJdbcOperations();
		int effectedRows = jdbcOperations.update(sql, sqlParameter);
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

	private boolean acceptedMethod(Method method) {
		Annotation[] annotations = method.getAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (acceptedAnnotation(annotation)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean acceptedAnnotation(Annotation annotation) {
		Class<?> annotationClass = annotation.annotationType();
		return annotationClass == Insert.class || annotationClass == Update.class || annotationClass == Get.class
				|| annotationClass == Select.class || annotationClass == Slice.class;
	}

	private SqlParameter getSqlParameter(Method method, Object[] args) {
		Annotation[][] annotations = method.getParameterAnnotations();
		Annotation[] requiredAnnotations = new Annotation[annotations.length];
		int i = 0;
		for (Annotation[] annotation : annotations) {
			requiredAnnotations[i++] = annotation[0];
		}
		int length = requiredAnnotations.length;
		Map<String, Object> parameters = new HashMap<String, Object>();
		if (length > 1) {
			Annotation annotation;
			for (i = 0; i < length; i++) {
				annotation = requiredAnnotations[i];
				if (annotation instanceof Arg) {
					parameters.put(((Arg) annotation).value(), args[i]);
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
		} else if (length == 1) {
			final Annotation annotation = requiredAnnotations[0];
			if (annotation instanceof Arg) {
				parameters.put(((Arg) annotation).value(), args[0]);
			} else if (annotation instanceof Example) {
				if (args[0] instanceof Map) {
					parameters.putAll((Map<String, Object>) args[0]);
				} else {
					parameters.putAll(PropertyUtils.convertToMap(args[0]));
				}
				String[] excludedProperties = ((Example) annotation).excludedProperties();
				if (ArrayUtils.isNotEmpty(excludedProperties)) {
					MapUtils.removeKeys(parameters, excludedProperties);
				}
			}
		}
		return new MapSqlParameter(parameters);
	}

	private JdbcOperations getJdbcOperations() {
		Session session = sessionManager.current();
		if (log.isTraceEnabled()) {
			log.trace(session.toString());
		}
		return session.getJdbcOperations();
	}

	public Class<T> getInterfaceClass() {
		return interfaceClass;
	}

}
