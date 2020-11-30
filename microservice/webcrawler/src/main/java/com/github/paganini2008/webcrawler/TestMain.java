package com.github.paganini2008.webcrawler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springdessert.webcrawler.jdbc.CatalogDao;

public class TestMain {

	public static void main(String[] args) {
		Method method= MethodUtils.getMethod(CatalogDao.class, "getCatalog", long.class);
		Parameter[] parameters = method.getParameters();
		System.out.println(parameters);
	}

}
