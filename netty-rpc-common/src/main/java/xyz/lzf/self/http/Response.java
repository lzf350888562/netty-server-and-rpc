package xyz.lzf.self.http;

import java.io.Serializable;

/**
 * rpc响应实体
 */
public class Response  implements Serializable {
    // 状态信息
    private int code;
    private String message;
    // 具体数据
    private Object data;

    public static Response success(Object data) {
        Response response = new Response();
        response.setCode(200);
        response.setData(data);
        return response;
    }
    public static Response fail() {
        Response response = new Response();
        response.setCode(500);
        response.setMessage("服务器未知错误!");
        return response;
    }
    public static Response fail(String msg) {
        Response response = new Response();
        response.setCode(500);
        response.setMessage(msg);
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
