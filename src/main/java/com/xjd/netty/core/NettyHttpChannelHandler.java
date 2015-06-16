package com.xjd.netty.core;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.xjd.netty.NettyHttpRequest;

/**
 * <pre>
 * Http请求的业务解析, 线程不安全
 * </pre>
 * @author elvis.xu
 * @since 2015-6-4
 */
public class NettyHttpChannelHandler extends SimpleChannelInboundHandler<Object> {
	private static Logger log = LoggerFactory.getLogger(NettyHttpChannelHandler.class);

	protected static HttpDataFactory dataFactory = new DefaultHttpDataFactory(true);

	@Autowired
	protected NettyRouterHander nettyRouterHander;

	protected NettyHttpRequest request;
	protected HttpPostRequestDecoder decoder;
	protected ByteBuffer buf;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;

			request = new NettyHttpRequest(httpRequest.getProtocolVersion(), httpRequest.getMethod(), httpRequest.getUri(),
					httpRequest.getDecoderResult().isSuccess());
			decoder = new HttpPostRequestDecoder(dataFactory, httpRequest);
			buf = ByteBuffer.allocate(0);

			// 其它设置
			request.setRemoteAddr(getRealRemoteAddr(ctx, msg));

			if (decoder.isMultipart()) {
				unsupportMultipart(ctx);
			}

		} else if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;

			if (httpContent.content().readableBytes() > 0) {
				ByteBuffer tmpBuf = ByteBuffer.allocate(buf.capacity() + httpContent.content().readableBytes());
				tmpBuf.put(buf.compact());
				tmpBuf.put(httpContent.content().nioBuffer());
				buf = tmpBuf;
			}

			if (msg instanceof LastHttpContent) {
				request.setEntity(buf.array());

				Object result = nettyRouterHander.route(request);

				if (result instanceof HttpResponse) {
					DefaultFullHttpResponse response = (DefaultFullHttpResponse) result;
					boolean keepAlive = HttpHeaders.isKeepAlive(request);
					if (keepAlive) {
						response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
						response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
					}
				}
				ctx.write(result);
			}
		}
	}

	protected String getRealRemoteAddr(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;

			String realAddress = httpRequest.headers().get("http_x_forwarded_for");
			if (StringUtils.isNotBlank(realAddress)) {
				return realAddress;
			}
		}
		SocketAddress socketAddress = ctx.channel().remoteAddress();
		if (socketAddress instanceof InetSocketAddress) {
			InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
			if (inetAddress != null) {
				return inetAddress.getHostAddress();
			}
		}

		log.warn("无法获取请求端的IP: " + socketAddress.toString());
		return socketAddress.toString();
	}

	protected void unsupportMultipart(ChannelHandlerContext ctx) {
		String s = "暂不支持上传文件";
		FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
				Unpooled.wrappedBuffer(s.getBytes(Charset.forName("utf8"))));
		res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "plain/text; charset=UTF-8");
		ctx.write(res);
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("netty exception caught: ", cause);
		ctx.close();
	}
}
