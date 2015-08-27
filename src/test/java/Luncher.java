import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-*.xml")
public class Luncher {
	private static Logger log = LoggerFactory.getLogger(Luncher.class);
	
	@Autowired
	ServerBootstrap bootstrap;

	@Test
	public void test() throws Exception {
		try {
			// 启动Netty
			ChannelFuture channelFuture = bootstrap.bind(9090).sync();

			log.info("server start at '{}'...", 9090);

			channelFuture.channel().closeFuture().sync();
		} finally {
			// 同步关闭套接字
			bootstrap.childGroup().shutdownGracefully();
			bootstrap.group().shutdownGracefully();
		}
	}
}
