package com.github.paganini2008.springworld.webcrawler;

import java.sql.SQLException;

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
		String url = "jdbc:mysql://192.168.159.131:3306/test?userUnicode=true&serverTimezone=GMT&characterEncoding=UTF8&useSSL=false&autoReconnect=true&zeroDateTimeBehavior=convertToNull";
		System.out.println(JdbcUtils.getConnection(url, "fengy", "123456"));
	}

}
