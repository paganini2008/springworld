package com.github.paganini2008.springworld.jdbcplus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.jdbcplus.ApplicationContextUtils;
import com.github.paganini2008.springworld.jdbcplus.DaoScannerRegistrar;
import com.github.paganini2008.springworld.jdbcplus.EnhancedJdbcTemplate;

/**
 * 
 * DaoScan 
 *
 * @author Fred Feng
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ EnhancedJdbcTemplate.class, DaoScannerRegistrar.class, ApplicationContextUtils.class })
public @interface DaoScan {

	String[] basePackages();

}
