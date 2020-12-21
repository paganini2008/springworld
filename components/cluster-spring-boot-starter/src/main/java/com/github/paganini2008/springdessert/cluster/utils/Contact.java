package com.github.paganini2008.springdessert.cluster.utils;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * 
 * Contact
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Data
@ConfigurationProperties("spring.application.cluster.contact")
public class Contact implements Serializable {

	private static final long serialVersionUID = 4110243793757357219L;
	private String name = "Jimmy Hoff";
	private String campany;
	private String department;
	private String position;
	private String homePage;
	private String email;
	private String phone;
	private String description;

}
