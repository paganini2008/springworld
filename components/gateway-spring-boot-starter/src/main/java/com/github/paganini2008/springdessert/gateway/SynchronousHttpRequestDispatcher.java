package com.github.paganini2008.springdessert.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.springdessert.cluster.gateway.HttpRequestDispatcher;
import com.github.paganini2008.springdessert.cluster.gateway.HttpRequestDispatcherSupport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * 
 * SynchronousHttpRequestDispatcher
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Sharable
public class SynchronousHttpRequestDispatcher extends HttpRequestDispatcherSupport implements HttpRequestDispatcher {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		FullHttpRequest httpRequest = (FullHttpRequest) data;
		HttpHeaders httpHeaders = getHeaders(httpRequest);
		MediaType mediaType = httpHeaders.getContentType();
		String path = httpRequest.uri();
		ByteBuf byteBuf = httpRequest.content();
		byte[] content = null;
		int length = byteBuf.readableBytes();
		if (length > 0) {
			content = new byte[length];
			byteBuf.readBytes(content);
		}
		path = "http://localhost:9097" + path;
		RequestEntity<byte[]> requestEntity = new RequestEntity<byte[]>(content, httpHeaders,
				HttpMethod.valueOf(httpRequest.method().name()), new URI(path));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		ByteBuf buffer = Unpooled.copiedBuffer(responseEntity.getBody(), CharsetUtil.UTF_8);
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(),
				HttpResponseStatus.valueOf(responseEntity.getStatusCodeValue()), buffer);
		response.headers().set(CONTENT_TYPE, "application/json");
		response.headers().set(CONTENT_LENGTH, buffer.readableBytes());
		response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "*");
		response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
		response.headers().set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		ctx.writeAndFlush(response);
		ctx.channel().close();
	}

	private HttpHeaders getHeaders(FullHttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

}
