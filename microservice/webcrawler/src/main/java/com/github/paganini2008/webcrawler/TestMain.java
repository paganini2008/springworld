package com.github.paganini2008.webcrawler;

import java.util.UUID;

import org.springframework.http.MediaType;

public class TestMain {

	public static void main(String[] args) {
		MediaType mediaType= MediaType.APPLICATION_JSON;
		System.out.println(mediaType.toString());
		System.out.println(mediaType.getType());
		System.out.println(UUID.randomUUID().toString());
	}

}
