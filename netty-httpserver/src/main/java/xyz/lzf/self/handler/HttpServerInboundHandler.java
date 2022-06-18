package xyz.lzf.self.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.lzf.self.handler.request.RequestHandler;
import xyz.lzf.self.handler.request.RequestHandlerFactory;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 处理Http请求的真正ChannelHandler
 * 查看ChannelHandler继承树, 寻找便携子类实现
 * 查看ChannelInboundHandlerAdapter注释, 选择自动释放消息的SimpleChannelInboundHandler
 */
public class HttpServerInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final Logger logger = LogManager.getLogger(HttpServerInboundHandler.class);

    private String faviconIco = "/favicon.ico";
    private AsciiString CONNECTION = AsciiString.cached("Connection");
    private AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
    private AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        logger.info("Handle http request:{}", msg);
        String uri = msg.uri();
        if (uri.equals(faviconIco)) {
            return;
        }
        RequestHandler requestHandler = RequestHandlerFactory.create(msg.method());
        Object result;
        FullHttpResponse response;
        try {
            result = requestHandler.handle(msg);
            String responseHtml = "<html><body><h1> Response from HttpServer :</h1><div>" + result + "<div/></body></html>";
            byte[] responseBytes = responseHtml.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        } catch (IllegalArgumentException e) {
            logger.error("see illegal argument:" + e);
            String responseHtml = "<html><body>" + e.toString() + "</body></html>";
            byte[] responseBytes = responseHtml.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");
        }
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Http server channel handler error : ", cause);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.error("Http server channel handler deal complete! ");
        ctx.flush();
    }
}
