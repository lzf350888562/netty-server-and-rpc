package xyz.lzf.self.handler.request;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 请求处理抽象
 */
public interface RequestHandler {
    Object handle(FullHttpRequest fullHttpRequest);
}
