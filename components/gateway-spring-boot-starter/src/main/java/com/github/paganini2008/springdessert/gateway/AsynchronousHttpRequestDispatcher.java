package com.github.paganini2008.springdessert.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.http.Request;
import com.github.paganini2008.springdessert.cluster.http.RequestInterceptorContainer;
import com.github.paganini2008.springdessert.cluster.http.RequestProcessor;
import com.github.paganini2008.springdessert.cluster.http.RestfulException;
import com.github.paganini2008.springdessert.cluster.http.RoutingAllocator;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * AsynchronousHttpRequestDispatcher
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Slf4j
@Sharable
public class AsynchronousHttpRequestDispatcher extends HttpRequestDispatcherSupport implements HttpRequestDispatcher {

	@Autowired
	private RequestProcessor requestProcessor;

	@Autowired
	private RequestInterceptorContainer requestInterceptorContainer;

	@Autowired
	private RoutingManager routingManager;

	@Autowired
	private RoutingAllocator routingAllocator;

	private final PathMatchedMap<String> staticResources = new PathMatchedMap<String>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		final FullHttpRequest httpRequest = (FullHttpRequest) data;
		String path = httpRequest.uri();
		String fullPath = "";
		String provider = staticResources.get(path);
		if (StringUtils.isNotBlank(provider)) {
			fullPath = provider + path;
		}
		Route route = routingManager.match(path);
		if (route != null) {
			String realPath = route.direct() ? path : path.substring(route.prefixEndPosition() + 1);
			fullPath = routingAllocator.allocateHost(route.provider(), realPath);
		}
		HttpHeaders httpHeaders = getHttpHeaders(httpRequest);
		if (MapUtils.isNotEmpty(route.defaultHeaders())) {
			httpHeaders.addAll(route.defaultHeaders());
		}
		if (CollectionUtils.isNotEmpty(route.ignoredHeaders())) {
			MapUtils.removeKeys(httpHeaders, route.ignoredHeaders());
		}
		MediaType mediaType = httpHeaders.getContentType();

		ByteBuf byteBuf = httpRequest.content();
		byte[] body = null;
		int length = byteBuf.readableBytes();
		if (length > 0) {
			body = new byte[length];
			byteBuf.readBytes(body);
		}
		RoutingRequest routingRequest = new RoutingRequest(fullPath, HttpMethod.valueOf(httpRequest.method().name()), httpHeaders, body);
		ResponseEntity<String> responseEntity = doSendRequest(routingRequest, route);

		ByteBuf buffer = Unpooled.copiedBuffer(responseEntity.getBody(), CharsetUtil.UTF_8);
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(),
				HttpResponseStatus.valueOf(responseEntity.getStatusCodeValue()), buffer);
		response.headers().set(CONTENT_TYPE, "application/json");
		response.headers().set(CONTENT_LENGTH, buffer.readableBytes());
		if (HttpUtil.isKeepAlive(httpRequest)) {
			response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		ctx.writeAndFlush(response);
		ctx.channel().close();
	}

	private ResponseEntity<String> doSendRequest(Request request, Route route) {
		ResponseEntity<String> responseEntity = null;
		Throwable reason = null;
		try {
			requestInterceptorContainer.beforeSubmit(request);
			int retries = route.retries();
			int timeout = route.timeout();
			int concurrency = route.concurrency();
			if (retries > 0 && timeout > 0) {
				responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(request, String.class, concurrency, retries, timeout);
			} else if (retries < 1 && timeout > 0) {
				responseEntity = requestProcessor.sendRequestWithTimeout(request, String.class, concurrency, timeout);
			} else if (retries > 0 && timeout < 1) {
				responseEntity = requestProcessor.sendRequestWithRetry(request, String.class, concurrency, retries);
			} else {
				responseEntity = requestProcessor.sendRequest(request, String.class, concurrency);
			}
		} catch (RestClientException e) {
			FallbackProvider fallback = getFallback(route.fallbackClass());
			if (fallback != null) {
				try {
					responseEntity = executeFallback(fallback, route, e, route.fallbackException(), route.fallbackHttpStatus());
					log.error(e.getMessage(), e);
				} catch (Exception fallbackError) {
					reason = fallbackError instanceof RestClientException ? (RestClientException) fallbackError
							: new RestfulException(fallbackError.getMessage(), fallbackError, request);
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			requestInterceptorContainer.afterSubmit(request, responseEntity, reason);
		}
		if (responseEntity != null) {
			return responseEntity;
		}

		if (reason != null) {
			if (reason instanceof RestClientException) {
				throw (RestClientException) reason;
			} else {
				throw new RestClientException(reason.getMessage(), reason);
			}
		}
		throw new RestClientException("Illegal request: " + request.toString());
	}

	private FallbackProvider getFallback(Class<?> fallbackClass) {
		try {
			if (fallbackClass != null) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(fallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private ResponseEntity<String> executeFallback(FallbackProvider fallback, Route route, RestClientException e,
			Class<? super Throwable>[] exceptionClasses, HttpStatus[] httpStatuses) {
		if (e instanceof RestfulException) {
			RestfulException restClientException = (RestfulException) e;
			for (Class<?> cls : exceptionClasses) {
				if (restClientException.getCause() != null && cls.isInstance(restClientException.getCause())) {
					return wrapResponse(fallback, route, restClientException);
				}
			}
		} else if (e instanceof HttpStatusCodeException) {
			HttpStatusCodeException restClientException = (HttpStatusCodeException) e;
			for (HttpStatus status : httpStatuses) {
				if (restClientException.getStatusCode() != null && restClientException.getStatusCode() == status) {
					return wrapResponse(fallback, route, restClientException);
				}
			}
		}
		throw e;
	}

	private ResponseEntity<String> wrapResponse(FallbackProvider fallback, Route route, RestClientException restClientException) {
		String body = fallback.getBody(route, restClientException);
		return new ResponseEntity<String>(body, fallback.getHeaders(), fallback.getHttpStatus());
	}

	private HttpHeaders getHttpHeaders(FullHttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

}
