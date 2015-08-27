package com.xjd.netty.load;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.xjd.netty.core.HttpRequestRouter;
import com.xjd.netty.core.NettyHttpChannelHandler;

@Configuration
public class SpringLoad {

	@Bean
	@Resource(name = "channelInitializer")
	public ServerBootstrap serverBootstrapFactory(ChannelInitializer<SocketChannel> channelInitializer) {
		// 配置服务器
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(channelInitializer)
				.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

		return serverBootstrap;
	}

	@Bean(name = "channelInitializer")
	public ChannelInitializer<SocketChannel> initializerFactory(final ApplicationContext contxt) {
		return new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				SimpleChannelInboundHandler<?> channelInboundHandler = contxt.getBean(NettyHttpChannelHandler.class);
				ChannelPipeline pipeline = ch.pipeline();
				// HTTP
				pipeline.addLast(new HttpRequestDecoder());
				pipeline.addLast(new HttpResponseEncoder());
				pipeline.addLast(new HttpContentCompressor());
				pipeline.addLast(new ChunkedWriteHandler());
				// 设置处理的Handler
				pipeline.addLast(channelInboundHandler);
			}
		};
	}

	@Bean
	@Resource(name = "httpRequestRouter")
	@Scope("prototype")
	public NettyHttpChannelHandler channelHandlerFactory(HttpRequestRouter httpRequestRouter) {
		return new NettyHttpChannelHandler(httpRequestRouter);
	}

	@Bean(name = "httpRequestRouter")
	public HttpRequestRouter routerFactory() {
		return new HttpRequestRouter();
	}

}
