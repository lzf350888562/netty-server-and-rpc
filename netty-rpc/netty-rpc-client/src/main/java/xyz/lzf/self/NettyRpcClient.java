package xyz.lzf.self;

import xyz.lzf.self.entity.User;
import xyz.lzf.self.proxy.NettyRpcServiceProxy;
import xyz.lzf.self.service.UserService;

public class NettyRpcClient {
    public static void main(String[] args) {
        NettyRpcServiceProxy nettyRpcServiceProxy = new NettyRpcServiceProxy();
        UserService userService = nettyRpcServiceProxy.getProxy(UserService.class);

        User userByUserId = userService.getUserByUserId(10);
        System.out.println("从服务端得到的user为：" + userByUserId);
    }
}
