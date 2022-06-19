package xyz.lzf.self.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.AttributeKey;
import xyz.lzf.self.core.RegisterCenter;
import xyz.lzf.self.handler.ClientChannelHandler;
import xyz.lzf.self.http.Request;
import xyz.lzf.self.http.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * rpc接口代理
 */
public class NettyRpcServiceProxy implements InvocationHandler {

    private RegisterCenter registerCenter;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    public NettyRpcServiceProxy() {
        this.registerCenter = new RegisterCenter();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setParamsTypes(method.getParameterTypes());
        Response response = sendRequest(request);
        if (response != null){
            return response.getData();
        }else{
            return null;
        }
    }

    /**
     * 利用本身作为JDK代理的参数, 生成代理类的接口
     */
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    private Response sendRequest(Request request) {
        InetSocketAddress address = registerCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 消息格式 [长度][消息体], 解决粘包问题
                            // 见https://www.cnblogs.com/rickiyang/p/12904552.html
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            // 计算当前待发送消息的长度，写入到前4个字节中
                            pipeline.addLast(new LengthFieldPrepender(4));

                            // todo JDK序列化 效率很差
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(new ClassResolver() {
                                @Override
                                public Class<?> resolve(String className) throws ClassNotFoundException {
                                    return Class.forName(className);
                                }
                            }));
                            // 核心, 从负载均衡中心拉取服务
                            pipeline.addLast(new ClientChannelHandler());
                        }
                    });
            ChannelFuture channelFuture  = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
            AttributeKey<Response> key = AttributeKey.valueOf("RPCResponse");
            Response response = channel.attr(key).get();
            return response;
        } catch (InterruptedException e) {
            // log todo
            return null;
        }
    }
}
