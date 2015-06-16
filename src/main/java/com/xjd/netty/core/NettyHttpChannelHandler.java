package com.xjd.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.xjd.netty.NettyHttpRequest;

/**
 * <pre>
 * Http请求的业务解析, 线程不安全
 * </pre>
 * @author elvis.xu
 * @since 2015-6-4
 */
public class NettyHttpChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static Logger log = LoggerFactory.getLogger(NettyHttpChannelHandler.class);

	protected HttpRequestRouter httpRequestRouter;
	protected NettyHttpRequest request;
	protected HttpPostRequestDecoder decoder;
	protected ByteBuf buf;

	public NettyHttpChannelHandler(HttpRequestRouter httpRequestRouter) {
		Assert.notNull(httpRequestRouter);
		this.httpRequestRouter = httpRequestRouter;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		log.trace("messaage received: {}", msg.getClass());

		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			
			NettyHttpRequest nettyHttpRequest = new 

			log.trace("uri: {}", httpRequest.getUri());
			log.trace("protocol: {}", httpRequest.getProtocolVersion().toString());
			log.trace("method: {}", httpRequest.getMethod().toString());
			log.trace("decoderResult: {}", httpRequest.getDecoderResult().toString());
			log.trace("remoteAddress: {}", ctx.channel().remoteAddress());
			log.trace("localAddress: {}", ctx.channel().localAddress());

			log.trace("[[headers]]");
			HttpHeaders headers = httpRequest.headers();
			for (Entry<String, String> header : headers.entries()) {
				log.trace("{}:{}", header.getKey(), header.getValue());
			}

			log.trace("[[cookies]]");
			Set<Cookie> cookies = null;
			String cookieStr = headers.get(HttpHeaders.Names.COOKIE);
			if (cookieStr == null) {
				cookies = Collections.emptySet();
			} else {
				cookies = CookieDecoder.decode(cookieStr);
			}
			for (Cookie cookie : cookies) {
				log.trace("Cookie: {}", cookie.toString());
			}

			log.trace("[[parameters]]");
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
			log.trace("uri: {}", queryStringDecoder.uri());
			log.trace("path: {}", queryStringDecoder.path());
			Map<String, List<String>> params = queryStringDecoder.parameters();
			for (Entry<String, List<String>> entry : params.entrySet()) {
				log.trace("{}:{}", entry.getKey(), Arrays.toString(entry.getValue().toArray()));
			}

			if (httpRequest.getMethod() == HttpMethod.POST) {
				log.trace("isChunked: {}", HttpHeaders.isTransferEncodingChunked(httpRequest));
				log.trace("isMulti: {}", HttpPostRequestDecoder.isMultipart(httpRequest));

				HttpDataFactory httpDataFactory;
				if (HttpPostRequestDecoder.isMultipart(httpRequest)) {
					httpDataFactory = new DefaultHttpDataFactory(true); // use disk
				} else {
					httpDataFactory = new DefaultHttpDataFactory(); // use mixed
				}
				try {
					httpPostRequestDecoder = new HttpPostRequestDecoder(httpDataFactory, httpRequest);
				} catch (ErrorDataDecoderException e) {
					log.error("cannot reolve request.");
					resolveError(ctx);
					return;
				}
			}

		} else if (msg instanceof HttpContent) {
			HttpContent chunk = (HttpContent) msg;
			try {
				httpPostRequestDecoder.offer(chunk);
			} catch (ErrorDataDecoderException e) {
				log.error("cannot reolve request.");
				resolveError(ctx);
				return;
			}

			log.trace("[[DATA]]");
			while (httpPostRequestDecoder.hasNext()) {
				InterfaceHttpData interfaceHttpData = httpPostRequestDecoder.next();
				if (interfaceHttpData.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) interfaceHttpData;
					log.trace("attribute: {}", attribute.toString());
				} else if (interfaceHttpData.getHttpDataType() == HttpDataType.FileUpload) {
					FileUpload fileUpload = (FileUpload) interfaceHttpData;
					log.trace("fileupload: {}", fileUpload.toString());
				}
			}

			if (chunk instanceof LastHttpContent) {
				log.trace("OK");
				// 应答
				// 应答完成后
				reset();
			}
		}
	}

}
