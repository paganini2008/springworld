package com.github.paganini2008.springworld.jobsoup.ui.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.jobsoup.ui.UIModel;
import com.github.paganini2008.springworld.jobsoup.ui.WebUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * WebMvcConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(true).setUseTrailingSlashMatch(true);
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.parse("100MB"));
		factory.setMaxRequestSize(DataSize.parse("100MB"));
		return factory.createMultipartConfig();
	}

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

	@Bean
	public HandlerInterceptor signHandlerInterceptor() {
		return new SignHandlerInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicHandlerInterceptor()).addPathPatterns("/**");
		registry.addInterceptor(signHandlerInterceptor()).addPathPatterns("/**");
	}

	/**
	 * 
	 * BasicHandlerInterceptor
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public static class BasicHandlerInterceptor implements HandlerInterceptor, EnvironmentAware {

		private static final String ATTR_WEB_CONTEXT_PATH = "contextPath";

		private Environment environment;

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			HttpSession session = request.getSession();
			if (session.getAttribute(ATTR_WEB_CONTEXT_PATH) == null) {
				String webContextPath = environment.getProperty("cronkeeper.ui.webContextPath");
				if (StringUtils.isBlank(webContextPath)) {
					webContextPath = WebUtils.getContextPath(request);
				}
				session.setAttribute(ATTR_WEB_CONTEXT_PATH, webContextPath);
			}
			return true;
		}

		public void setEnvironment(Environment environment) {
			this.environment = environment;
		}

	}

	/**
	 * 
	 * SignHandlerInterceptor
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	@Slf4j
	public static class SignHandlerInterceptor implements HandlerInterceptor {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			request.setAttribute("sign", System.currentTimeMillis());
			if (log.isTraceEnabled()) {
				log.trace(request.toString());
			}
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			if (log.isTraceEnabled()) {
				long startTime = (Long) request.getAttribute("sign");
				log.trace("Path: " + request.getServletPath() + " take(ms): " + (System.currentTimeMillis() - startTime));
			}
		}

	}

	/**
	 * 
	 * NormalResponsePreHandler
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	@Slf4j
	@RestControllerAdvice
	public static class NormalResponsePreHandler implements ResponseBodyAdvice<Object> {

		@Override
		public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
			return true;
		}

		@Override
		public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
				Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest req, ServerHttpResponse resp) {
			ServletServerHttpRequest request = (ServletServerHttpRequest) req;
			HttpServletRequest servletRequest = request.getServletRequest();
			long startTime = 0;
			if (log.isTraceEnabled()) {
				startTime = (Long) servletRequest.getAttribute("sign");
				log.trace("Path: " + servletRequest.getServletPath() + " take(ms): " + (System.currentTimeMillis() - startTime));
			}
			if (body instanceof UIModel) {
				UIModel<?> resultVO = (UIModel<?>) body;
				resultVO.setElapsed(startTime > 0 ? System.currentTimeMillis() - startTime : 0);
				resultVO.setRequestPath(servletRequest.getServletPath());
			}
			return body;
		}

	}
}
