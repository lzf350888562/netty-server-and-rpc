package xyz.lzf.self.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.lzf.self.http.Response;

/**
 * 选择ChannelHandler的自动释放消息的SimpleChannelInboundHandler子类
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<Response> {
    public static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        logger.info("receive msg. save to channel attribute 'RPCResponse'");
        AttributeKey<Response> key = AttributeKey.valueOf("RPCResponse");
        ctx.channel().attr(key).set(msg);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("ChannelHandlerContext error : ", cause);
        ctx.close();
    }
}