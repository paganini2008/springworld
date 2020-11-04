package com.github.paganini2008.springworld.jobsoup;

import org.slf4j.Logger;

import com.github.paganini2008.devtools.TableArray;

/**
 * 
 * Banner
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class Banner {

	public static final String PRODUCT_NAME = "JobSoup";

	public static final String PRODUCT_VERSION = "2.0-RC5";

	public static final String PRODUCT_DESCRIPTION = "A light job schedule framework and workflow tools writen in Java";

	public static void printBanner(String deployMode, Logger logger) {
		TableArray tableArray = new TableArray(6, 2);
		tableArray.setWidth(0, 30);
		tableArray.setWidth(1, 70);
		tableArray.setValueOnRight(1, 0, "[my name]: ", 1);
		tableArray.setValueOnLeft(1, 1, PRODUCT_NAME, 1);
		tableArray.setValueOnRight(2, 0, "[my description]: ", 1);
		tableArray.setValueOnLeft(2, 1, PRODUCT_DESCRIPTION, 1);
		tableArray.setValueOnRight(3, 0, "[my version]: ", 1);
		tableArray.setValueOnLeft(3, 1, PRODUCT_VERSION, 1);
		tableArray.setValueOnRight(4, 0, "[current deploy mode]: ", 1);
		tableArray.setValueOnLeft(4, 1, deployMode, 1);
		String[] lines = tableArray.toStringArray(false, false);
		for (String line : lines) {
			logger.info(line);
		}
	}

}
