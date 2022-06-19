package xyz.lzf.self.proxy.request.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.lzf.self.proxy.request.RequestHandler;

import java.nio.charset.StandardCharsets;

public class PutRequestHandler implements RequestHandler {
    public static final Logger logger = LoggerFactory.getLogger(PutRequestHandler.class);

    @Override
    public Object handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        logger.info("deal with PUT request uri :[ {} ]", requestUri);
        String contentType = this.getContentType(fullHttpRequest.headers());
        if (contentType.equals("application/json")) {
            return fullHttpRequest.content().toString(StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("only receive application/json type data");
        }

    }

    private String getContentType(HttpHeaders headers) {
        String typeStr = headers.get("Content-Type");
        String[] list = typeStr.split(";");
        return list[0];
    }
}
