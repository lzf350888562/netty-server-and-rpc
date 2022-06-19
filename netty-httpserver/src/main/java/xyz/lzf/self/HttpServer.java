package xyz.lzf.self;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.lzf.self.proxy.HttpServerInboundHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HttpServer {
    public static final Logger logger = LoggerFactory.getLogger(HttpServer.class);


    private int port = 8080;

    public HttpServer() {
        // 要加 '/' 否则无法读取, 见注释
        InputStream is = HttpServer.class.getResourceAsStream("/web.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            this.port = Integer.parseInt((String)properties.get("server.port")) ;
        } catch (IOException e) {
            logger.warn("see invalid property 'server.port' in 'web.properties'. use default port on 8080");
            this.port = 8080;
        }
    }

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // 类似ServerSocket
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 一直保持连接活动状态, 开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 等待处理的客户端连接请求大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置父类AbstractBootstrap的handler属性
                    .handler( new LoggingHandler(LogLevel.INFO))
                    // 设置当前类ServerBootstrap的childHandler属性
                    // 给Channel设置handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
//                                    .addLast("logging", new LoggingHandler(LogLevel.INFO))
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                                    .addLast("handler", new HttpServerInboundHandler());
                        }
                    });
            Channel ch = b.bind(port).sync().channel();
            logger.info("Netty sttp server started on port {}.", port);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Error in netty http server starting :", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
