import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
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
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
	private static Logger log = LoggerFactory.getLogger(Test.class);

	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new HttpChannelInitializer());

			Channel channel = sb.bind(9001).sync().channel();

			log.info("server started at: 9001");

			channel.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	public static class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();

			pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
			pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
			pipeline.addLast("httpContentCompressor", new HttpContentCompressor());
			pipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
			pipeline.addLast("httpHandler", new HttpHandler());
		}

	}

	public static class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {

		protected HttpPostRequestDecoder httpPostRequestDecoder;

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			if (httpPostRequestDecoder != null) {
				try {
					httpPostRequestDecoder.cleanFiles();
					httpPostRequestDecoder = null;
				} catch (Exception e) {
					log.warn("", e);
				}
			}
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
			log.trace("messaage received: {}", msg.getClass().getName());

			if (msg instanceof HttpRequest) {
				HttpRequest httpRequest = (HttpRequest) msg;

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
					DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
					response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
					ctx.write(response);
					HttpChunkedInput httpChunkWriter;
					try {
						httpChunkWriter = new HttpChunkedInput(new ChunkedFile(new File("/tmp/tmp.txt")));
						ctx.write(httpChunkWriter, ctx.newProgressivePromise()).addListener(
								new ChannelProgressiveFutureListener() {

									@Override
									public void operationComplete(ChannelProgressiveFuture future) throws Exception {
										System.out.println("FINISH!");
									}

									@Override
									public void operationProgressed(ChannelProgressiveFuture future, long progress, long total)
											throws Exception {
										System.out.println(progress + ":" + total);
									}

								});

					} catch (IOException e) {
						e.printStackTrace();
					}
					// ctx.write(new
					// DefaultHttpContent(Unpooled.wrappedBuffer("HELLO".getBytes(Charset.forName("utf8")))));
					// ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
					ctx.flush();
					reset();
				}
			}
		}

		protected void resolveError(ChannelHandlerContext ctx) {
			// 返回错误
			reset();
			ctx.channel().close();
		}

		protected void reset() {
			if (httpPostRequestDecoder != null) {
				try {
					httpPostRequestDecoder.destroy();
					httpPostRequestDecoder = null;
				} catch (Exception e) {
					log.warn("", e);
				}
			}
		}
	}

}
