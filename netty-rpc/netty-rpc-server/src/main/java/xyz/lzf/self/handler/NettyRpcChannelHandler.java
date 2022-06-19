package xyz.lzf.self.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import xyz.lzf.self.core.RegisterCenter;
import xyz.lzf.self.http.Request;
import xyz.lzf.self.http.Response;
import xyz.lzf.self.route.ServerRouter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NettyRpcChannelHandler extends SimpleChannelInboundHandler<Request> {

    private ServerRouter serverRouter;

    public NettyRpcChannelHandler(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {
        Response response = getResponse(msg);
        ctx.writeAndFlush(response);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    Response getResponse(Request request) {
        String interfaceName = request.getInterfaceName();
        Object service = serverRouter.getService(interfaceName);
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object invoke = method.invoke(service, request.getParams());
            return Response.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();//todo
            return Response.fail();
        }
    }
}
