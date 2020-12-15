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
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.http.FallbackProvider;
import com.github.paganini2008.springdessert.cluster.http.Request;
import com.github.paganini2008.springdessert.cluster.http.RequestProcessor;
import com.github.paganini2008.springdessert.cluster.http.RequestTemplate;
import com.github.paganini2008.springdessert.cluster.http.RestfulException;
import com.github.paganini2008.springdessert.cluster.http.RoutingAllocator;
import com.github.paganini2008.springdessert.cluster.http.SimpleRequest;
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

	private final PathMatchedMap<String> staticResources = new PathMatchedMap<String>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		final FullHttpRequest httpRequest = (FullHttpRequest) data;
		final String path = httpRequest.uri();
		HttpHeaders httpHeaders = copyHttpHeaders(httpRequest);
		String fullPath = "";
		String provider = staticResources.get(path);
		if (StringUtils.isNotBlank(provider)) {
			fullPath = provider + path;
		}
		Router router = null;
		if (StringUtils.isBlank(fullPath)) {
			router = routingManager.match(path);
			if (router != null) {
				String realPath = router.direct() ? path : path.substring(router.prefixEndPosition() + 1);
				fullPath = routingAllocator.allocateHost(router.provider(), realPath);
			}
		}
		
		if (router != null) {
			if (MapUtils.isNotEmpty(router.defaultHeaders())) {
				httpHeaders.addAll(router.defaultHeaders());
			}
			if (CollectionUtils.isNotEmpty(router.ignoredHeaders())) {
				MapUtils.removeKeys(httpHeaders, router.ignoredHeaders());
			}
		}
		MediaType mediaType = httpHeaders.getContentType();
		if (mediaType == null) {
			mediaType = MediaType.APPLICATION_JSON;
		}

		ByteBuf byteBuf = httpRequest.content();
		byte[] body = null;
		int length = byteBuf.readableBytes();
		if (length > 0) {
			body = new byte[length];
			byteBuf.readBytes(body);
		}
		SimpleRequest request = new SimpleRequest(fullPath, HttpMethod.valueOf(httpRequest.method().name()), httpHeaders, body);
		request.setAttribute("timeout", router.timeout());
		request.setAttribute("retries", router.retries());
		request.setAttribute("permits", router.permits());
		request.setAttribute("fallback", getFallback(router.fallback()));
		ResponseEntity<String> responseEntity;
		try {
			 responseEntity = requestTemplate.sendRequest(provider, request, String.class);
		} finally {
			request.clearAttributes();
		}
		
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
