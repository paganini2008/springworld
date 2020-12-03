package com.github.paganini2008.springdessert.jobsoup;

import org.slf4j.Logger;

import com.github.paganini2008.devtools.TableView;

/**
 * 
 * Banner
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public abstract class Banner {

	public static final String PRODUCT_NAME = "JobSoup";

	public static final String PRODUCT_VERSION = "2.0-RC5";

	public static final String PRODUCT_DESCRIPTION = "A light job schedule framework and workflow tools writen in Java";

	public static void printBanner(String deployMode, Logger logger) {
		TableView tableView = new TableView(6, 2);
		tableView.setWidth(0, 30);
		tableView.setWidth(1, 70);
		tableView.setValueOnRight(1, 0, "[my name]: ", 0);
		tableView.setValueOnLeft(1, 1, PRODUCT_NAME, 0);
		tableView.setValueOnRight(2, 0, "[my description]: ", 0);
		tableView.setValueOnLeft(2, 1, PRODUCT_DESCRIPTION, 0);
		tableView.setValueOnRight(3, 0, "[my version]: ", 0);
		tableView.setValueOnLeft(3, 1, PRODUCT_VERSION, 0);
		tableView.setValueOnRight(4, 0, "[current deploy mode]: ", 0);
		tableView.setValueOnLeft(4, 1, deployMode, 0);
		String[] lines = tableView.toStringArray(false, false);
		for (String line : lines) {
			logger.info(line);
		}
	}

}
