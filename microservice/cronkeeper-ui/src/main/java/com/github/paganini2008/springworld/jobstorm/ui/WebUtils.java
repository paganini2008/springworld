package com.github.paganini2008.springworld.jobstorm.ui;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * WebUtils
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class WebUtils {

	public static String getCurrentIpAddress() {
		return getIpAddress(getRequest());
	}

	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		int index = ip.indexOf(",");
		if (index != -1) {
			return ip.substring(0, index);
		}
		return ip;
	}

	public static String getHostUrl(String url) {
		String hostUrl = "";
		try {
			URL u = new URL(url);
			hostUrl = u.getProtocol() + "://" + u.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return hostUrl;
	}

	public static String getContextPath(HttpServletRequest request) {
		return getHostUrl(request.getRequestURL().toString()) + ":" + request.getServerPort() + request.getContextPath();
	}

	public static HttpServletRequest getRequiredRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	}

	public static HttpServletRequest getRequest() {
		try {
			return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Cookie getCookie(String name) {
		HttpServletRequest request = getRequest();
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

}
