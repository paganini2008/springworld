package com.github.paganini2008.springdessert.jobsoup.ui;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * UIModel
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class UIModel<T> {
	
	private boolean success;
	private String msg;
	private T data;
	private String errorMsg;
	private String[] errorDetail;
	private long elapsed;
	private String requestPath;
	
	public UIModel() {
	}
	
	public static <T> UIModel<T> success(T data){
		return success(data, "ok");
	}

	public static <T> UIModel<T> success(T data, String msg) {
		UIModel<T> model = new UIModel<T>();
		model.setSuccess(true);
		model.setMsg(msg);
		model.setData(data);
		return model;
	}

	public static <T> UIModel<T> failure(String msg) {
		UIModel<T> model = new UIModel<T>();
		model.setSuccess(false);
		model.setMsg(msg);
		return model;
	}

}
