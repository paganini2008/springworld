package com.github.paganini2008.springdessert.logbox.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.paganini2008.devtools.net.UrlUtils;

/**
 * 
 * WebMvcConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/META-INF/static/");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(true).setUseTrailingSlashMatch(true);
	}

	@ConditionalOnMissingBean
	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowCredentials(true);
		corsConfiguration.addAllowedHeader("*");
		corsConfiguration.addAllowedOrigin("*");
		corsConfiguration.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", corsConfiguration);
		return new CorsFilter(source);
	}

	@Bean
	public HandlerInterceptor basicHandlerInterceptor() {
		return new BasicHandlerInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicHandlerInterceptor()).addPathPatterns("/**");
	}

	/**
	 * 
	 * BasicHandlerInterceptor
	 * 
	 * @author Jimmy Hoff
	 *
	 * @since 1.0
	 */
	public static class BasicHandlerInterceptor implements HandlerInterceptor {

		private static final String WEB_ATTRIBUTE_CONTEXT_PATH = "contextPath";

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			HttpSession session = request.getSession();
			if (session.getAttribute(WEB_ATTRIBUTE_CONTEXT_PATH) == null) {
				session.setAttribute(WEB_ATTRIBUTE_CONTEXT_PATH, getContextPath(request));
			}
			return true;
		}

		private String getContextPath(HttpServletRequest request) {
			return UrlUtils.toHostUrl(request.getRequestURL().toString()) + request.getContextPath();
		}

	}
}
