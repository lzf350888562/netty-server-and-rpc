package xyz.lzf.self;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import xyz.lzf.self.handler.NettyRpcChannelHandler;
import xyz.lzf.self.route.ServerRouter;
import xyz.lzf.self.service.impl.DeptServiceImpl;
import xyz.lzf.self.service.impl.UserServiceImpl;

/**
 * dubbo好像是消费者定时从注册中心拉取可用服务列表缓存, 即使注册中心不可用, 服务也可以调用
 * 这里简化将服务器与服务提供者放在一起, 服务器开放一个端口SERVER_PORT, 启动前注册自己的服务到zk
 * 可启动多个NettyRpcServer, 然后开启客户端调用
 */
public class NettyRpcServer {

    private static final int SERVER_PORT = 8080; //这里可以修改端口启动多个尝试

    public static void main(String[] args) {
        ServerRouter serverRouter = new ServerRouter("localhost", SERVER_PORT);
        // 注册几个服务
        registerServices(serverRouter);

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 消息格式 [长度][消息体], 解决粘包问题
                            // 见https://www.cnblogs.com/rickiyang/p/12904552.html
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            // 计算当前待发送消息的长度，写入到前4个字节中
                            pipeline.addLast(new LengthFieldPrepender(4));

                            // todo
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(new ClassResolver() {
                                @Override
                                public Class<?> resolve(String className) throws ClassNotFoundException {
                                    return Class.forName(className);
                                }
                            }));
                            // 核心, 从负载均衡中心拉取服务
                            pipeline.addLast(new NettyRpcChannelHandler(serverRouter));
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(SERVER_PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private static void registerServices(ServerRouter serverRouter) {
        serverRouter.provideServiceInterface(new UserServiceImpl());
        serverRouter.provideServiceInterface(new DeptServiceImpl());
    }
}
