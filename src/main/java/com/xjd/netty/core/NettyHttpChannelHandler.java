package com.xjd.netty.core;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xjd.netty.HttpResponse;

/**
 * <pre>
 * Http请求的业务解析, 线程不安全
 * </pre>
 * @author elvis.xu
 * @since 2015-6-4
 */
public class NettyHttpChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static Logger log = LoggerFactory.getLogger(NettyHttpChannelHandler.class);

	protected static HttpDataFactory httpDataFactory = new DefaultHttpDataFactory();

	protected HttpRequestRouter router;
	protected NettyHttpRequest request;
	protected HttpPostRequestDecoder decoder;
	protected CompositeByteBuf buf;

	public NettyHttpChannelHandler(HttpRequestRouter httpRequestRouter) {
		if (httpRequestRouter == null) {
			throw new RuntimeException("httpRequestRouter cannot be null.");
		}
		this.router = httpRequestRouter;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		log.trace("messaage received: {}", msg.getClass());

		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;

			request = new NettyHttpRequest(httpRequest);
			request.setLocalAddress(ctx.channel().localAddress());
			request.setRemoteAddress(ctx.channel().remoteAddress());

			Collection<Cookie> cookies = null;
			String cookieStr = request.headers().get(HttpHeaders.Names.COOKIE);
			if (cookieStr == null) {
				cookies = Collections.emptySet();
			} else {
				cookies = CookieDecoder.decode(cookieStr);
			}

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if (httpRequest.getMethod() == HttpMethod.POST) {
				Map<String, List<String>> ps = new HashMap<String, List<String>>();
				ps.putAll(params);
				request.setParameters(ps);
			} else {
				request.setParameters(params);
			}

			if (httpRequest.getMethod() == HttpMethod.POST) {
				request.setMultipart(HttpPostRequestDecoder.isMultipart(httpRequest));
			}

			if (request.isMultipart()) {
				request.setUploadedFiles(new LinkedList<FileUpload>());
			} else {
				request.setUploadedFiles(Collections.<FileUpload> emptyList());
			}

			HttpResponse response = router.supportRequest(request); // 是否支持该请求的处理

			if (response != null) { // 不支持
				write(ctx, response);
				reset();
				ctx.channel().close();
				return;
			}

			if (httpRequest.getMethod() == HttpMethod.POST) {
				if (request.isMultipart() || isFormData(request.getHeaders().get(HttpHeaders.Names.CONTENT_TYPE))) {
					try {
						decoder = new HttpPostRequestDecoder(httpDataFactory, httpRequest);
					} catch (ErrorDataDecoderException e) {
						log.error("cannot reolve request.", e);
						decodeError(ctx, request);
						return;
					}
				}
			}

			if (httpRequest.getMethod() == HttpMethod.POST && decoder == null) {// 不需要decoder使用Bytebuf
				buf = Unpooled.compositeBuffer(); // 注意大小只有16个
			}

		} else if (msg instanceof HttpContent) {
			HttpContent chunk = (HttpContent) msg;

			if (decoder != null) {
				try {
					decoder.offer(chunk);
					decodeAttributes(decoder, request);
				} catch (ErrorDataDecoderException e) {
					log.error("cannot reolve request.", e);
					decodeError(ctx, request);
					return;
				} catch (IOException e) {
					log.error("cannot reolve request.", e);
					decodeError(ctx, request);
					return;
				}
			} else if (buf != null) { // 不需要decoder
				buf.addComponent(chunk.content());
			}

			if (chunk instanceof LastHttpContent) {
				
			}
		}
	}

	protected void write(ChannelHandlerContext ctx, HttpResponse res) {

	}

	protected void reset() {
		if (decoder != null) {
			try {
				decoder.destroy();
			} catch (Exception e) {
				log.warn("", e);
			}
			decoder = null;
		}
		if (buf != null) {
			buf = null;
		}
		if (request != null) {
			request = null;
		}
	}

	protected void decodeError(ChannelHandlerContext ctx, NettyHttpRequest request) {
		NettyHttpResponse response = new NettyHttpResponse();
		response.setStatus(HttpResponseStatus.BAD_REQUEST);
		response.setCookies(request.getCookies());
		DefaultHttpHeaders headers = new DefaultHttpHeaders();
		headers.set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf8");
		response.setHeaders(headers);
		write(ctx, response);
		reset();
		ctx.channel().close();
	}

	protected boolean isFormData(String contentType) {
		if (contentType == null || contentType.trim().equals("")) {
			return false;
		}
		if (contentType.toLowerCase().startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)) {
			return true;
		}
		return false;
	}

	protected void decodeAttributes(HttpPostRequestDecoder decoder, NettyHttpRequest request) throws IOException {
		while (decoder.hasNext()) {
			InterfaceHttpData interfaceHttpData = decoder.next();
			if (interfaceHttpData.getHttpDataType() == HttpDataType.Attribute) {
				Attribute attribute = (Attribute) interfaceHttpData;
				String name = attribute.getName();
				String value = attribute.getValue();

				List<String> attrValues = request.getParameters().get(name);
				if (attrValues == null) {
					attrValues = new LinkedList<String>();
					request.getParameters().put(name, attrValues);
				}
				attrValues.add(value);

			} else if (interfaceHttpData.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) interfaceHttpData;

				request.getUploadedFiles().add(fileUpload);
			}
		}
	}
}
