package com.github.paganini2008.springworld.webcrawler;

import java.sql.SQLException;
import java.util.UUID;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;

public class TestJdbc {
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException {
		//String url = "jdbc:mysql://192.168.159.138:3306/?userUnicode=true&serverTimezone=GMT&characterEncoding=UTF8&useSSL=false&autoReconnect=true&zeroDateTimeBehavior=convertToNull";
		//System.out.println(JdbcUtils.getConnection(url, "root", "root123"));
		System.out.println(UUID.randomUUID().toString());
	}

}
