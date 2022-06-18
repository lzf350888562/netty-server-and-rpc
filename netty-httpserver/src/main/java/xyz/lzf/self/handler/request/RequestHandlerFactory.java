package xyz.lzf.self.handler.request;

import io.netty.handler.codec.http.HttpMethod;
import xyz.lzf.self.handler.request.impl.DeleteRequestHandler;
import xyz.lzf.self.handler.request.impl.GetRequestHandler;
import xyz.lzf.self.handler.request.impl.PostRequestHandler;
import xyz.lzf.self.handler.request.impl.PutRequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 *  简单工厂模式获取具体请求处理器
 */
public class RequestHandlerFactory {
    public static final Map<HttpMethod, RequestHandler> REQUEST_HANDLERS = new HashMap<>();

    static {
        REQUEST_HANDLERS.put(HttpMethod.GET, new GetRequestHandler());
        REQUEST_HANDLERS.put(HttpMethod.POST, new PostRequestHandler());
        REQUEST_HANDLERS.put(HttpMethod.DELETE, new DeleteRequestHandler());
        REQUEST_HANDLERS.put(HttpMethod.PUT, new PutRequestHandler());
    }

    public static RequestHandler create(HttpMethod httpMethod) {
        return REQUEST_HANDLERS.get(httpMethod);
    }
}
