package com.github.paganini2008.springdessert.cluster.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.http.FallbackProvider;
import com.github.paganini2008.springdessert.cluster.http.ForwardedRequest;
import com.github.paganini2008.springdessert.cluster.http.RequestTemplate;
import com.github.paganini2008.springdessert.cluster.http.RestClientUtils;
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
	private RequestTemplate requestTemplate;

	@Autowired
	private RoutingAllocator routingAllocator;

	@Autowired
	private RoutingManager routingManager;

	@Autowired
	private Cache cache;

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		final FullHttpRequest httpRequest = (FullHttpRequest) data;
		final String path = httpRequest.uri();
		Router router = routingManager.match(path);
		String shortPath = router.direct() ? path : path.substring(router.prefixEndPosition() + 1);
		String fullPath = routingAllocator.allocateHost(router.provider(), shortPath);
		ResponseEntity<String> responseEntity = null;
		if (router.cached()) {
			responseEntity = (ResponseEntity<String>) cache.getObject(fullPath, () -> {
				return doSendRequest(httpRequest, router, fullPath);
			});
		}
		if (responseEntity == null) {
			responseEntity = doSendRequest(httpRequest, router, fullPath);
		}
		ByteBuf buffer = Unpooled.copiedBuffer(responseEntity.getBody(), CharsetUtil.UTF_8);
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(httpRequest.protocolVersion(),
				HttpResponseStatus.valueOf(responseEntity.getStatusCodeValue()), buffer);
		httpResponse.headers().set(CONTENT_LENGTH, buffer.readableBytes());
		MediaType mediaType = responseEntity.getHeaders().getContentType();
		if (mediaType == null) {
			mediaType = MediaType.ALL;
		}
		httpResponse.headers().set(CONTENT_TYPE, mediaType.toString());
		if (HttpUtil.isKeepAlive(httpRequest)) {
			httpResponse.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		ctx.writeAndFlush(httpResponse);
		ctx.channel().close();
	}

	private ResponseEntity<String> doSendRequest(FullHttpRequest httpRequest, Router router, String fullPath) {
		HttpHeaders httpHeaders = copyHttpHeaders(httpRequest);
		if (MapUtils.isNotEmpty(router.defaultHeaders())) {
			httpHeaders.addAll(router.defaultHeaders());
		}
		if (CollectionUtils.isNotEmpty(router.ignoredHeaders())) {
			MapUtils.removeKeys(httpHeaders, router.ignoredHeaders());
		}
		ByteBuf byteBuf = httpRequest.content();
		byte[] body = null;
		int length = byteBuf.readableBytes();
		if (length > 0) {
			body = new byte[length];
			byteBuf.readBytes(body);
		}
		ForwardedRequest request = new ForwardedRequest(fullPath, HttpMethod.valueOf(httpRequest.method().name()), httpHeaders);
		request.setBody(body);
		request.setTimeout(router.timeout());
		request.setRetries(router.retries());
		request.setAllowedPermits(router.allowedPermits());
		request.setFallback(getFallback(router.fallback()));

		ResponseEntity<String> responseEntity;
		try {
			responseEntity = requestTemplate.sendRequest(router.provider(), request, String.class);
		} catch (Throwable e) {
			responseEntity = RestClientUtils.getErrorResponse(e);
		}
		return responseEntity;
	}

	private FallbackProvider getFallback(Class<?> fallbackClass) {
		try {
			if (fallbackClass != null && fallbackClass != Void.class && fallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(fallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private HttpHeaders copyHttpHeaders(FullHttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

}
